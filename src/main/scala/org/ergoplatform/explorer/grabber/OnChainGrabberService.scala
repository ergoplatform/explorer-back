package org.ergoplatform.explorer.grabber

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.dao.HeadersDao
import org.ergoplatform.explorer.db.models.{BlockInfo, Header}
import org.ergoplatform.explorer.grabber.db.{BlockInfoWriter, DBHelper}
import org.ergoplatform.explorer.grabber.http.{NodeAddressService, RequestServiceImpl}
import org.ergoplatform.explorer.grabber.protocol.{ApiFullBlock, ApiNodeInfo}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

final class OnChainGrabberService(
  xa: Transactor[IO],
  config: ExplorerConfig
)(implicit protected val ec: ExecutionContext)
  extends LoopedIO {

  private val blockInfoHelper: BlockInfoHelper = new BlockInfoHelper(config.protocol)

  protected val logger = Logger("on-chain-grabber-service")
  protected val pause: FiniteDuration = config.grabber.onChainPollDelay

  private val addressServices = config.grabber.nodes.map(NodeAddressService)
  private val mandatoryAddressService = addressServices.head
  private val requestService = new RequestServiceImpl[IO]

  private val dBHelper = new DBHelper(config.protocol)
  private val headersDao = new HeadersDao

  private val MaxRetriesNumber = 5

  private def idsAtHeight(height: Long): IO[List[String]] =
    requestService.get[List[String]](mandatoryAddressService.idsAtHeightUri(height))

  private def readFullBlock(id: String): IO[Option[ApiFullBlock]] = {
    def readWithRetry(attemptsDone: Int): IO[Option[ApiFullBlock]] = {
      if (attemptsDone < addressServices.size) {
        val currentService = addressServices(attemptsDone)
        requestService.getSafe[ApiFullBlock](currentService.fullBlockUri(id)).flatMap {
          case Left(f) =>
            logger.error(
              s"Error while requesting block with id = $id from ${currentService.nodeAddress}",
              f
            )
            readWithRetry(attemptsDone + 1)
          case Right(v) =>
            IO.pure(Some(v))
        }
      } else {
        IO.pure(None)
      }
    }
    readWithRetry(0)
  }

  //Looking for a block. In case of error just move forward.
  private def fullBlocksForIds(ids: List[String]): IO[List[ApiFullBlock]] =
    ids
      .parTraverse(readFullBlock)
      .map(_.flatten.toList)

  private def writeBlocksFromHeight(
    height: Long,
    existingHeadersAtHeight: List[Header] = List.empty,
    attemptsDone: Int = 0
  ): IO[List[BlockInfo]] = {
    def updatedBlock(b: ApiFullBlock, isMain: Boolean) =
      b.copy(header = b.header.copy(mainChain = isMain))
    for {
      ids    <- idsAtHeight(height)
      blocks <- fullBlocksForIds(ids.filterNot(existingHeadersAtHeight.map(_.id).contains))
      existingHeadersUpdated <- IO.pure(
        existingHeadersAtHeight.map(h => h.copy(mainChain = ids.headOption.contains(h.id)))
      )
      _ <- {
        if (ids.size > existingHeadersAtHeight.size)
          headersDao.updateMany(existingHeadersUpdated).transact[IO](xa)
        else IO.unit
      }
      blockInfos <- blocks.map { block =>
        writeBlock(updatedBlock(block, ids.headOption.contains(block.header.id)))
      }.parSequence
      retryNeeded <- IO.pure(
        blocks.isEmpty && attemptsDone < MaxRetriesNumber && ids.size > existingHeadersAtHeight.size
      )
      _ <- IO {
        val retryInfo = if (retryNeeded) "Retrying.." else ""
        logger.info(s"${blocks.length} block(s) from height $height has been written. $retryInfo")
      }
      _ <- IO.suspend(
        if (retryNeeded) writeBlocksFromHeight(height, existingHeadersAtHeight, attemptsDone + 1)
        else IO.unit
      )
    } yield blockInfos
  }

  private def writeBlock(apiBlock: ApiFullBlock): IO[BlockInfo] =
    for {
      blockInfo <- {
        if (apiBlock.header.height == Constants.GenesisHeight)
          IO.pure(blockInfoHelper.assembleGenesisInfo(apiBlock))
        else
          getBlockInfo(apiBlock.header.parentId, apiBlock.header.height - 1)
            .map(blockInfoHelper.assembleNonGenesisInfo(apiBlock, _))
      }
      _ <- IO(blockInfoHelper.blockInfoCache.put(blockInfo.headerId, blockInfo))
      _ <- (dBHelper.writeOne(apiBlock) *> BlockInfoWriter.insert(blockInfo)).transact[IO](xa)
    } yield blockInfo

  private def getBlockInfo(id: String, height: Long): IO[BlockInfo] =
    for {
      blockInfoFromCacheOpt <- IO(blockInfoHelper.blockInfoCache.getIfPresent(id))
      blockInfo <- IO.suspend {
        blockInfoFromCacheOpt match {
          case Some(blockInfoFromCache) => IO(blockInfoFromCache)
          case None =>
            BlockInfoWriter.get(id).transact[IO](xa).flatMap {
              case Some(blockInfoFromDb) =>
                IO(blockInfoHelper.blockInfoCache.put(id, blockInfoFromDb))
                  .map(_ => blockInfoFromDb)
              case None => // fork occurred, mark blocks we have at this height as non-best
                IO(logger.info(s"Chain differs at height: $height")).flatMap { _ =>
                  headersDao.getAtHeight(height).transact[IO](xa).flatMap { existingHeaders =>
                    writeBlocksFromHeight(height, existingHeaders).map(_.head)
                  }
                }
            }
        }
      }
    } yield blockInfo

  protected val task: IO[Unit] = for {
    _             <- IO(logger.info("Starting sync task.."))
    info          <- requestService.get[ApiNodeInfo](mandatoryAddressService.infoUri)
    currentHeight <- dBHelper.readCurrentHeight.transact(xa)
    _ <- IO {
      logger.info(s"Current full height in db: $currentHeight")
      logger.info(s"Current full height on node ${info.fullHeight}")
    }
    heightsRange <- IO {
      if (currentHeight == info.fullHeight) List.empty[Long]
      else Range.Long.inclusive(currentHeight + 1, info.fullHeight, 1L).toList
    }
    _ <- heightsRange.traverse(writeBlocksFromHeight(_))
    _ <- IO {
      logger.info(s"Sync task has been finished. Current height now is ${info.fullHeight}.")
    }
  } yield ()

}
