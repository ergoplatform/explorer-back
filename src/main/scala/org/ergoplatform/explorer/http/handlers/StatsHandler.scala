package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

import scala.concurrent.duration._

class StatsHandler(ss: StatsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("stats") {
    withRequestTimeout(180.seconds) {
      forksR ~ statsR
    }
  }

  def statsR: Route = (pathEndOrSingleSlash & get) {
    ss.findLastStats
  }

  def forksR: Route = (path("forks") & get & parameters("fromHeight".as[Long] ? -1L)) {
    ss.forksInfo
  }

}
