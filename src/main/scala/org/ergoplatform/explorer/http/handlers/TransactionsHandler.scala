package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.services.TransactionsService

class TransactionsHandler(txs: TransactionsService[IO]) extends FailFastCirceSupport with CommonDirectives {

  val route = pathPrefix("transactions") {
    getTxById
  }

  def getTxById = (get & base58IdPath) { id =>
    val f = txs.getTxInfo(id).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

}
