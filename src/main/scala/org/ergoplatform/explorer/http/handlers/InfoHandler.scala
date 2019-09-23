package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

final class InfoHandler(ss: StatsService[IO]) extends RouteHandler {

  val route: Route = (pathPrefix("info") & get) {
    ss.findBlockchainInfo
  }

}
