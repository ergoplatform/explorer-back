package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.services.StatsService

class ChartsHandler(ss: StatsService[IO]) extends FailFastCirceSupport with CommonDirectives {

  val route = pathPrefix("charts") {
    totalCoins ~ avgBlockSize ~ marketPrice
  }

  val totalCoins = (get & pathPrefix("total") & duration) { d =>
    val f = ss.totalCoinsForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val avgBlockSize = (get & pathPrefix("block-size") & duration) { d =>
    val f = ss.avgBlockSizeForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val marketPrice = (get & pathPrefix("market-price") & duration) { d =>
    val f = ss.marketPriceUsdForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

}
