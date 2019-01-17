package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

import scala.concurrent.duration._

class StatsHandler(ss: StatsService[IO]) extends RouteHandler {

  val route: Route = (pathPrefix("stats") & get) {
    withRequestTimeout(180.seconds) {
      ss.findLastStats
    }
  }

}
