package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.Json
import org.ergoplatform.explorer.services.TransactionsService

class TransactionsHandler(service: TransactionsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("transactions") {
    submitTransaction ~
      getUnconfirmed ~
      getUnspentOutputsByErgoTree ~
      getUnspentOutputsByAddress ~
      getOutputsByErgoTree ~
      getOutputsByAddress ~
      getOutputsById ~
      getTxById
  }

  def submitTransaction: Route = (post & entity(as[Json])) {
    service.submitTransaction
  }

  def getTxById: Route = (get & base16Segment) {
    service.getTxInfo
  }

  def getUnconfirmed: Route = (pathPrefix("unconfirmed") & get) {
    service.getUnconfirmed
  }

  def getOutputsById: Route = (pathPrefix("boxes") & base16Segment) {
    service.getOutputById
  }

  def getOutputsByErgoTree: Route = (pathPrefix("boxes" / "byErgoTree") & base16Segment) {
    service.getOutputsByErgoTree(_)
  }

  def getOutputsByAddress: Route = (pathPrefix("boxes" / "byAddress") & base58Segment) {
    service.getOutputsByAddress(_)
  }

  def getUnspentOutputsByErgoTree: Route = (pathPrefix("boxes" / "byErgoTree" / "unspent") & base16Segment) {
    service.getOutputsByErgoTree(_, unspentOnly = true)
  }

  def getUnspentOutputsByAddress: Route = (pathPrefix("boxes" / "byAddress" / "unspent") & base58Segment) {
    service.getOutputsByAddress(_, unspentOnly = true)
  }

}
