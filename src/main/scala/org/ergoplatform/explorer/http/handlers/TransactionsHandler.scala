package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.services.TransactionsService

class TransactionsHandler(service: TransactionsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("transactions") {
    getOutputsByErgoTree ~
      getOutputsByHash ~
      getTxById
  }

  def getTxById: Route = (get & base16Segment) {
    service.getTxInfo
  }

  def getOutputsByErgoTree: Route = (pathPrefix("boxes" / "byProposition") & base16Segment) {
    service.getOutputsByErgoTree
  }

  def getOutputsByHash: Route = (pathPrefix("boxes" / "byAddress") & base58Segment) {
    service.getOutputsByHash
  }

}
