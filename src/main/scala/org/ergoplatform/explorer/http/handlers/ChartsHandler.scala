package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.services.StatsService

class ChartsHandler(ss: StatsService[IO]) extends FailFastCirceSupport with CommonDirectives {

  val route = pathPrefix("charts") {
    totalCoins ~ avgBlockSize ~ blockChainSize ~ avgTxsPerBlock ~ avgDifficulty ~ minerRevenue
  }

  val totalCoins = (get & pathPrefix("total") & duration) { d =>
    val f = ss.totalCoinsForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val avgBlockSize = (get & pathPrefix("block-size") & duration) { d =>
    val f = ss.avgBlockSizeForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val blockChainSize = (get & pathPrefix("blockchain-size") & duration) { d =>
    val f = ss.avgBlockChainSizeForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val avgTxsPerBlock = (get & pathPrefix("transactions-per-block") & duration) { d =>
    val f = ss.avgTxsPerBlockForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val avgDifficulty = (get & pathPrefix("difficulty") & duration) { d =>
    val f = ss.avgDifficultyForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

  val minerRevenue = (get & pathPrefix("miners-revenue") & duration) { d =>
    val f = ss.minerRevenueForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

}
