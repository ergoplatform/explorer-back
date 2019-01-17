package org.ergoplatform.explorer.grabber

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.Logger
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.grabber.db.{BlockInfoWriter, DBHelper}
import org.ergoplatform.explorer.grabber.http.{NodeAddressService, RequestServiceImpl}
import org.ergoplatform.explorer.grabber.protocol.{ApiFullBlock, ApiNodeInfo}

import scala.concurrent.ExecutionContext

class GrabberService(xa: Transactor[IO], executionContext: ExecutionContext, config: ExplorerConfig) {

  private val blockInfoHelper: BlockInfoHelper = new BlockInfoHelper(config.network)

  implicit val ec: ExecutionContext = executionContext

  private val logger = Logger("grabber-service")

  private val addressService = NodeAddressService(config.grabber.nodes.head)
  private val requestService = new RequestServiceImpl[IO]

  private val dBHelper = new DBHelper(config.network)

  private val active = new AtomicBoolean(false)
  private val pause = config.grabber.pollDelay


  private def idsAtHeight(height: Long): IO[List[String]] =
    requestService.get[List[String]](addressService.idsAtHeightUri(height))

  private def fullBlocksSafe(id: String): IO[Option[ApiFullBlock]] = {
    requestService.getSafe[ApiFullBlock](addressService.fullBlockUri(id)).flatMap {
      case Left(f) =>
        logger.error(s"Error while requesting block with id $id", f)
        IO.pure(None)
      case Right(v) =>
        IO.pure(Some(v))
    }
  }

  //Looking for a block. In case of error just move forward.
  private def fullBlocksForIds(ids: List[String]): IO[List[(ApiFullBlock, Boolean)]] = ids
    .parTraverse(fullBlocksSafe)
    .map(_.flatten.toList)
    .map(_.zipWithIndex.map { case (bs, i) => bs -> (i == 0)})

  def writeBlocksFromHeight(h: Long): IO[Unit] = {
    def updatedBlock(b: ApiFullBlock, isMain: Boolean) = b.copy(header = b.header.copy(mainChain = isMain))
    for {
      ids <- idsAtHeight(h)
      blocks <- fullBlocksForIds(ids)
      _ <- blocks.map { case (block, isMain) =>
        dBHelper.writeOne(updatedBlock(block, isMain)).transact[IO](xa)
      }.parSequence
      _ <- blocks.map { case (block, isMain) =>
        writeBlockInfo(updatedBlock(block, isMain))
      }.parSequence
      _ <- IO {
        logger.info(s"${blocks.length} block(s) from height $h has been written to db")
      }
    } yield ()
  }

  private def writeBlockInfo(apiBlock: ApiFullBlock): IO[Unit] = for {
    _ <- if (apiBlock.header.height == Constants.GenesisHeight) IO.pure(()) else prepareCache(apiBlock.header.parentId)
    blockInfo <- IO { blockInfoHelper.extractBlockInfo(apiBlock) }
    _ <- BlockInfoWriter.insert(blockInfo).transact[IO](xa)
  } yield ()

  def prepareCache(id: String): IO[Unit] = for {
    maybePresented <- IO {
      blockInfoHelper.blockInfoCache.getIfPresent(id)
    }
    _ <- IO.suspend {
      maybePresented match {
        case Some(v) => IO { logger.trace(s"got block info from cache for height ${v.height}")}
        case None => BlockInfoWriter.get(id).transact(xa).map { bi =>
          logger.trace(s"not found in cache for id $id")
          blockInfoHelper.blockInfoCache.put(id, bi)
        }
      }
    }
  } yield ()

  private val sync: IO[Unit] = for {
    _ <- IO {
      logger.info("Starting sync task.")
    }
    info <- requestService.get[ApiNodeInfo](addressService.infoUri)
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
    _ <- heightsRange.map { writeBlocksFromHeight }.sequence[IO, Unit]
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
