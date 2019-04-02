package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.services.TransactionsService

class TransactionsHandler(service: TransactionsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("transactions") {
    getOutputsByProposition ~
      getOutputsByHash ~
      getTxById
  }

  def getTxById: Route = (get & base16Segment) {
    service.getTxInfo
  }

  def getOutputsByProposition: Route = (pathPrefix("boxes" / "byErgoTree") & base16Segment) { prop =>
    service.getOutputsByProposition(prop)
  }

  def getOutputsByHash: Route = (pathPrefix("boxes" / "byAddress") & base58Segment) { hash =>
    service.getOutputsByHash(hash)
  }

  def getUnspentOutputsByProposition: Route = (pathPrefix("boxes" / "byErgoTree" / "unspent") & base16Segment) { prop =>
    service.getOutputsByProposition(prop, unspentOnly = true)
  }

  def getUnspentOutputsByHash: Route = (pathPrefix("boxes" / "byAddress" / "unspent") & base58Segment) { hash =>
    service.getOutputsByHash(hash, unspentOnly = true)
  }

}
