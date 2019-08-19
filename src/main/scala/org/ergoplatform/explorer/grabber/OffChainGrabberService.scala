package org.ergoplatform.explorer.grabber

import cats.effect.IO
import cats.effect.concurrent.Ref
import com.typesafe.scalalogging.Logger
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.grabber.http.{NodeAddressService, RequestServiceImpl}
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.persistence.TransactionsPool

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

/** Unconfirmed transactions pool monitoring service.
  */
class OffChainGrabberService(txPoolRef: Ref[IO, TransactionsPool], config: ExplorerConfig)
                            (implicit protected val ec: ExecutionContext)
  extends LoopedIO {

  protected val logger = Logger("off-chain-grabber-service")

  protected val pause: FiniteDuration = config.grabber.offChainPollDelay

  private val requestService = new RequestServiceImpl[IO]

  private val addressServices = NodeAddressService(config.grabber.nodes.head)

  protected val task: IO[Unit] = for {
    _ <- IO(logger.info("Starting off-chain monitoring task."))
    txs <- requestService.get[List[ApiTransaction]](addressServices.memPoolUri)
  } yield txPoolRef.update(_ => TransactionsPool.empty.put(txs))

}
