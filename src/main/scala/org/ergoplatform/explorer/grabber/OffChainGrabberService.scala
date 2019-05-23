package org.ergoplatform.explorer.grabber

import java.util.concurrent.atomic.AtomicBoolean

import cats.effect.IO
import cats.implicits._
import com.typesafe.scalalogging.Logger
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.grabber.http.{NodeAddressService, RequestServiceImpl}
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.persistence.OffChainPersistence

import scala.concurrent.ExecutionContext

/** Performs off-chain monitoring */
class OffChainGrabberService(persistence: OffChainPersistence, config: ExplorerConfig)
                            (implicit ec: ExecutionContext) {

  private val logger = Logger("off-chain-grabber-service")

  private val active = new AtomicBoolean(false)

  private val pause = config.grabber.offChainPollDelay

  private val requestService = new RequestServiceImpl[IO]

  private val addressServices = NodeAddressService(config.grabber.nodes.head)

  private val syncTask: IO[Unit] = for {
    _ <- IO {
      logger.info("Starting off-chain monitoring task.")
    }
    txs <- requestService.get[List[ApiTransaction]](addressServices.memPoolUri)
  } yield persistence.put(txs)

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
    (IO.shift(ec) *> run(syncTask)).unsafeRunAsync { r =>
      logger.info(s"Off-chain grabber stopped. Cause: $r")
    }
  } else {
    logger.warn("Trying to start service that already has been started.")
  }

}
