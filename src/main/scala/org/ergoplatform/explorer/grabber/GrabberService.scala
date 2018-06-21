package org.ergoplatform.explorer.grabber

import java.util.concurrent.atomic.AtomicBoolean

import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import com.typesafe.scalalogging.Logger
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.config.GrabberConfig
import org.ergoplatform.explorer.grabber.db.DBHelper
import org.ergoplatform.explorer.grabber.models.{NodeFullBlock, NodeInfo}

import scala.concurrent.ExecutionContext

class GrabberService(xa: Transactor[IO], executionContext: ExecutionContext, grConfig: GrabberConfig) {

  implicit val ec: ExecutionContext = executionContext

  val logger = Logger("grabber-service")

  private val addressService = new NodeAddressService(grConfig.nodes.head)
  private val requestService = new RequestServiceImpl[IO]

  private val active = new AtomicBoolean(false)
  private val pause = grConfig.pollDelay


  private def idsAtHeight(height: Long): IO[List[String]] =
    requestService.get[List[String]](addressService.idsAtHeightUri(height))

  private def fullBlock(id: String): IO[NodeFullBlock] =
    requestService.get[NodeFullBlock](addressService.fullBlockUri(id))

  private def fullBlocksForIds(ids: List[String]): IO[List[NodeFullBlock]] = ids.parTraverse { id => fullBlock(id) }

  private def writeBlocksFromHeight(h: Long): IO[Unit] = for {
    ids <- idsAtHeight(h)
    blocks <- fullBlocksForIds(ids)
    _ <- blocks.map { b => DBHelper.writeOne(b).transact[IO](xa) }.parSequence
    _ <- IO {
      logger.info(s"${blocks.length} block(s) from height $h has been written to db")
    }
  } yield ()

  private val sync: IO[Unit] = for {
    _ <- IO {
      logger.info("Starting sync task.")
    }
    info <- requestService.get[NodeInfo](addressService.infoUri)
    currentHeight <- DBHelper.readCurrentHeight.transact(xa)
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

  private def run(io: IO[Unit]): IO[Unit] = io.attempt *> IO.sleep(pause) *> IO.suspend {
    if (active.get) {
      run(io)
    } else {
      IO.raiseError(new InterruptedException("Grabber service has been stopped"))
    }
  }

  def stop: Unit = {
    logger.info("Stopping service.")
    active.set(false)
  }

  def start: Unit = if (!active.get()) {
    active.set(true)
    (IO.shift(ec) *> run(sync)).unsafeRunAsync { r =>
      logger.info(s"Grabber stopped. Cause: $r")
    }
  } else {
    logger.warn("Trying to start service that already has benn started.")
  }
}
