package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import org.ergoplatform.explorer.services.TransactionsService

class TransactionsHandler(txs: TransactionsService[IO]) extends RouteHandler {

  val route = pathPrefix("transactions") {
    getTxById
  }

  def getTxById = (get & base16Segment) { id =>
    txs.getTxInfo(id)
  }

}
