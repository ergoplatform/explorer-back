package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.Json
import org.ergoplatform.explorer.services.TransactionsService

class TransactionsHandler(service: TransactionsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("transactions") {
    submitTransaction ~
      getOutputsByAddress ~
      getUnconfirmedByAddress ~
      getUnconfirmedTxById ~
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

  def getUnconfirmedTxById: Route = (pathPrefix("unconfirmed") & get & base16Segment) {
    service.getUnconfirmedTxInfo
  }

  def getUnconfirmed: Route = (pathPrefix("unconfirmed") & pathEndOrSingleSlash & get) {
    service.getUnconfirmed
  }

  def getUnconfirmedByAddress: Route = (pathPrefix("unconfirmed" / "byAddress") & get & base58Segment) {
    service.getUnconfirmedByAddress
  }

  def getOutputsById: Route = (pathPrefix("boxes") & get & base16Segment) {
    service.getOutputById
  }

  def getOutputsByErgoTree: Route = (pathPrefix("boxes" / "byErgoTree") & get & base16Segment) {
    service.getOutputsByErgoTree(_)
  }

  def getOutputsByAddress: Route = (pathPrefix("boxes" / "byAddress") & get & base58Segment) {
    service.getOutputsByAddress(_)
  }

  def getUnspentOutputsByErgoTree: Route = (pathPrefix("boxes" / "byErgoTree" / "unspent") & get & base16Segment) {
    service.getOutputsByErgoTree(_, unspentOnly = true)
  }

  def getUnspentOutputsByAddress: Route = (pathPrefix("boxes" / "byAddress" / "unspent") & get & base58Segment) {
    service.getOutputsByAddress(_, unspentOnly = true)
  }

}
