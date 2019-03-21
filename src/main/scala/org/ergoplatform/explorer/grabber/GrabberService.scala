package org.ergoplatform.explorer.grabber

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.models.BlockInfo
import org.ergoplatform.explorer.grabber.db.{BlockInfoWriter, DBHelper}
import org.ergoplatform.explorer.grabber.http.{NodeAddressService, RequestServiceImpl}
import org.ergoplatform.explorer.grabber.protocol.{ApiFullBlock, ApiNodeInfo}

import scala.concurrent.ExecutionContext

class GrabberService(xa: Transactor[IO], executionContext: ExecutionContext, config: ExplorerConfig) {

  private val blockInfoHelper: BlockInfoHelper = new BlockInfoHelper(config.network)

  implicit val ec: ExecutionContext = executionContext

  private val logger = Logger("grabber-service")

  private val addressServices = config.grabber.nodes.map(NodeAddressService)
  private val mandatoryAddressService = addressServices.head
  private val requestService = new RequestServiceImpl[IO]

  private val dBHelper = new DBHelper(config.network)

  private val active = new AtomicBoolean(false)
  private val pause = config.grabber.pollDelay
  private val MaxRetriesNumber = 4

  private def idsAtHeight(height: Long): IO[List[String]] =
    requestService.get[List[String]](mandatoryAddressService.idsAtHeightUri(height))

  private def fullBlocksSafe(id: String): IO[Option[ApiFullBlock]] = {
    def tryGet(numRetries: Int = 0): IO[Option[ApiFullBlock]] = {
      if (numRetries < addressServices.size) {
        val currentService = addressServices(numRetries)
        requestService.getSafe[ApiFullBlock](currentService.fullBlockUri(id)).flatMap {
          case Left(f) =>
            logger.error(s"Error while requesting block with id = $id from ${currentService.nodeAddress}", f)
            tryGet(numRetries + 1)
          case Right(v) =>
            IO.pure(Some(v))
        }
      } else {
        IO.pure(None)
      }
    }
    tryGet()
  }

  //Looking for a block. In case of error just move forward.
  private def fullBlocksForIds(ids: List[String]): IO[List[(ApiFullBlock, Boolean)]] = ids
    .parTraverse(fullBlocksSafe)
    .map(_.flatten.toList)
    .map(_.zipWithIndex.map { case (bs, i) => bs -> (i == 0)})

  def writeBlocksFromHeight(height: Long,
                            excluding: List[String] = List.empty,
                            retries: Int = 0): IO[List[BlockInfo]] = {
    def updatedBlock(b: ApiFullBlock, isMain: Boolean) = b.copy(header = b.header.copy(mainChain = isMain))
    for {
      ids <- idsAtHeight(height)
      blocks <- fullBlocksForIds(ids)
      blockInfos <- blocks.map { case (block, isMain) =>
        writeBlock(updatedBlock(block, isMain))
      }.parSequence
      retryNeeded <- IO(blocks.isEmpty && retries < MaxRetriesNumber)
      _ <- IO {
        val retryInfo = if (retryNeeded) "Retrying.." else ""
        logger.info(s"${blocks.length} block(s) from height $height has been written. $retryInfo")
      }
      _ <- IO.suspend(if (retryNeeded) writeBlocksFromHeight(height, excluding, retries + 1) else IO.unit)
    } yield blockInfos
  }

  private def writeBlock(apiBlock: ApiFullBlock): IO[BlockInfo] = for {
    blockInfo <- {
      if (apiBlock.header.height == Constants.GenesisHeight) IO(blockInfoHelper.assembleGenesisInfo(apiBlock))
      else getBlockInfo(apiBlock.header.parentId, apiBlock.header.height - 1)
        .map(blockInfoHelper.assembleNonGenesisInfo(apiBlock, _))
    }
    _ <- BlockInfoWriter.insert(blockInfo).flatMap(_ => dBHelper.writeOne(apiBlock)).transact[IO](xa)
  } yield blockInfo

  private def getBlockInfo(id: String, height: Long): IO[BlockInfo] = for {
    blockInfoFromCacheOpt <- IO(blockInfoHelper.blockInfoCache.getIfPresent(id))
    blockInfo <- IO.suspend {
      blockInfoFromCacheOpt match {
        case Some(blockInfoFromCache) => IO(blockInfoFromCache)
        case None => BlockInfoWriter.get(id).transact[IO](xa).flatMap {
          case Some(blockInfoFromDb) =>
            blockInfoHelper.blockInfoCache.put(id, blockInfoFromDb)
            IO(blockInfoFromDb)
          case None => // todo: fork occurred, mark blocks we have at this height as non-best
            writeBlocksFromHeight(height).map(_.head)
        }
      }
    }
  } yield blockInfo

  // todo: remove
  def prepareCache(id: String): IO[Unit] = for {
    maybePresented <- IO {
      blockInfoHelper.blockInfoCache.getIfPresent(id)
    }
    _ <- IO.suspend {
      maybePresented match {
        case Some(v) => IO { logger.trace(s"got block info from cache for height ${v.height}")}
        case None => BlockInfoWriter.get(id).transact(xa).map {
          case Some(blockInfo) =>
            logger.trace(s"block $id not found in cache")
            blockInfoHelper.blockInfoCache.put(id, blockInfo)
          case None =>
            // todo: handle fork
        }
      }
    }
  } yield ()

  private val sync: IO[Unit] = for {
    _ <- IO {
      logger.info("Starting sync task.")
    }
    info <- requestService.get[ApiNodeInfo](mandatoryAddressService.infoUri)
    currentHeight <- dBHelper.readCurrentHeight.transact(xa)
    _ <- IO {
      logger.info(s"Current full height in db: $currentHeight")
      logger.info(s"Current full height on node ${info.fullHeight}")
    }
    heightsRange <- IO {
      if (currentHeight == info.fullHeight) {
        List.empty[Long]
      } else {
        Range.Long.inclusive(currentHeight + 1, info.fullHeight, 1L).toList
      }
    }
    _ <- heightsRange.map(writeBlocksFromHeight(_)).sequence[IO, Unit]
    _ <- IO {
      logger.info(s"Sync task has been finished. Current height now is ${info.fullHeight}.")
    }
  } yield ()

  private def run(io: IO[Unit]): IO[Unit] = io.attempt.flatMap {
    case Right(_) => IO {
      ()
    }
    case Left(f) => IO {
      logger.error("An error has occurred: ", f)
    }
  } *> IO.sleep(pause) *> IO.suspend {
    if (active.get) {
      run(io)
    } else {
      IO.raiseError(new InterruptedException("Grabber service has been stopped"))
    }
  }

  def stop(): Unit = {
    logger.info("Stopping service.")
    active.set(false)
  }

  def start(): Unit = if (!active.get()) {
    active.set(true)
    (IO.shift(ec) *> run(sync)).unsafeRunAsync { r =>
      logger.info(s"Grabber stopped. Cause: $r")
    }
  } else {
    logger.warn("Trying to start service that already has been started.")
  }

}
