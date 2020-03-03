package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.Json
import org.ergoplatform.explorer.services.TransactionsService
import org.ergoplatform.explorer.utils.Paging

final class TransactionsHandler(service: TransactionsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("transactions") {
    submitTransaction ~
    getTxsSince ~
    getUnconfirmedByAddress ~
    getUnconfirmedTxById ~
    getUnconfirmed ~
    getUnspentOutputsByErgoTree ~
    getUnspentOutputsByErgoTreeTemplate ~
    getUnspentOutputsByAddress ~
    getOutputsByErgoTree ~
    getOutputsByErgoTreeTemplate ~
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

  def getTxsSince: Route = (pathPrefix("since") & intSegment & get & paging) {
    (height, offset, limit) =>
      service.getTxsSince(height, Paging(offset, limit))
  }

  def getUnconfirmedTxById: Route = (pathPrefix("unconfirmed") & get & base16Segment) {
    service.getUnconfirmedTxInfo
  }

  def getUnconfirmed: Route = (pathPrefix("unconfirmed") & pathEndOrSingleSlash & get) {
    service.getUnconfirmed
  }

  def getUnconfirmedByAddress: Route =
    (pathPrefix("unconfirmed" / "byAddress") & get & base58Segment) {
      service.getUnconfirmedByAddress
    }

  def getOutputsById: Route = (pathPrefix("boxes") & get & base16Segment) {
    service.getOutputById
  }

  def getOutputsByErgoTree: Route =
    (pathPrefix("boxes" / "byErgoTree") & get & base16Segment) {
      service.getOutputsByErgoTree(_)
    }

  def getOutputsByErgoTreeTemplate: Route =
    (pathPrefix("boxes" / "byErgoTreeTemplate") & get & base16Segment) {
      service.getOutputsByErgoTreeTemplate(_)
    }

  def getOutputsByAddress: Route =
    (pathPrefix("boxes" / "byAddress") & get & base58Segment) {
      service.getOutputsByAddress(_)
    }

  def getUnspentOutputsByErgoTree: Route =
    (pathPrefix("boxes" / "byErgoTree" / "unspent") & get & base16Segment) {
      service.getOutputsByErgoTree(_, unspentOnly = true)
    }

  def getUnspentOutputsByErgoTreeTemplate: Route =
    (pathPrefix("boxes" / "byErgoTreeTemplate" / "unspent") & get & base16Segment) {
      service.getOutputsByErgoTreeTemplate(_, unspentOnly = true)
    }

  def getUnspentOutputsByAddress: Route =
    (pathPrefix("boxes" / "byAddress" / "unspent") & get & base58Segment) {
      service.getOutputsByAddress(_, unspentOnly = true)
    }

}
