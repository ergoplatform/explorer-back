package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.HttpEntity
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.services.StatsService

final class InfoHandler(ss: StatsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("info") {
    totalSupply ~ info
  }

  def info: Route = (pathEndOrSingleSlash & get) {
    ss.findBlockchainInfo
  }

  // This is special method producing `text/plain` response required for exchanges.
  // Do not change it!
  def totalSupply: Route = (path("supply") & get) {
    onSuccess(ss.findBlockchainInfo.unsafeToFuture())(
      result =>
        complete(
          HttpEntity(
            BigDecimal
              .apply(result.supply.toDouble / Constants.CoinsInOneErgo)
              .setScale(Constants.ErgoDecimalPlacesNum)
              .toString()
          )
      )
    )
  }

}
