package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

class InfoHandler(ss: StatsService[IO]) extends ApiRoute {

  val route = (pathPrefix("info") & get) {
    ss.findBlockchainInfo
  }

}
