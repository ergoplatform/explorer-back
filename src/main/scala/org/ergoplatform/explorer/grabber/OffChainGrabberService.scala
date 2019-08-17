package org.ergoplatform.explorer.grabber

import cats.effect.concurrent.Ref
import cats.effect.{IO, Timer}
import cats.implicits._
import com.typesafe.scalalogging.Logger
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.grabber.http.{NodeAddressService, RequestServiceImpl}
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.persistence.TransactionsPool

import scala.concurrent.ExecutionContext

/** Unconfirmed transactions pool monitoring service.
  */
class OffChainGrabberService(txPoolRef: Ref[IO, TransactionsPool], config: ExplorerConfig)
                            (implicit ec: ExecutionContext) {

  private implicit val timer: Timer[IO] = IO.timer(ec)

  private val logger = Logger("off-chain-grabber-service")

  private val pause = config.grabber.offChainPollDelay

  private val requestService = new RequestServiceImpl[IO]

  private val addressServices = NodeAddressService(config.grabber.nodes.head)

  private val syncTask: IO[Unit] = for {
    _ <- IO(logger.info("Starting off-chain monitoring task."))
    txs <- requestService.get[List[ApiTransaction]](addressServices.memPoolUri)
  } yield txPoolRef.update(_ => TransactionsPool.empty.put(txs))

  private def run(io: IO[Unit]): IO[Unit] =
    io.attempt.flatMap {
      case Right(_) => IO.unit
      case Left(f) => IO(logger.error("An error has occurred: ", f))
    } *> IO.sleep(pause) *> IO.suspend(run(io))

  def start: IO[Unit] = IO.shift(ec) *> run(syncTask)

}
