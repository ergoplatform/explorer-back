package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.db.models.StatRecord
import org.ergoplatform.explorer.http.protocol.StatsSummary
import org.ergoplatform.explorer.services.StatsService

class StatsHandler(ss: StatsService[IO]) extends FailFastCirceSupport {

  val emptyStatsResponse = StatsSummary.apply(StatRecord())

  val route = (pathPrefix("stats") & get) {
    val f = ss.findLastStats.unsafeToFuture()
    onSuccess(f) {
      case Some(stats) => complete(stats)
      case None => complete(emptyStatsResponse)
    }
  }
}
