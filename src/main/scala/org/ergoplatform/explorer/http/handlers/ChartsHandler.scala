package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

class ChartsHandler(ss: StatsService[IO]) extends ApiRoute {

  val route = pathPrefix("charts") {
    totalCoins ~ avgBlockSize ~ blockChainSize ~ avgTxsPerBlock ~ avgDifficulty ~ minerRevenue ~ hashrate
  }

  val totalCoins = (get & pathPrefix("total") & duration) { d =>
    ss.totalCoinsForDuration(d)
  }

  val avgBlockSize = (get & pathPrefix("block-size") & duration) { d =>
    ss.avgBlockSizeForDuration(d)
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

  val hashrate = (get & pathPrefix("hash-rate") & duration) { d =>
    val f = ss.hashrateForDuration(d).unsafeToFuture()
    onSuccess(f) { result => complete(result) }
  }

}
