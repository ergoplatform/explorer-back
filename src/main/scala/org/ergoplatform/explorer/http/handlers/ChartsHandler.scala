package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

class ChartsHandler(ss: StatsService[IO]) extends RouteHandler {

  val route = pathPrefix("charts") {
    totalCoins ~
    avgBlockSize ~
    blockChainSize ~
    avgTxsPerBlock ~
    avgDifficulty ~
    minerRevenue ~
    hashrateDistribution ~
    hashrate
  }

  val totalCoins = (get & pathPrefix("total") & duration) { d =>
    ss.totalCoinsForDuration(d)
  }

  val avgBlockSize = (get & pathPrefix("block-size") & duration) { d =>
    ss.avgBlockSizeForDuration(d)
  }

  val blockChainSize = (get & pathPrefix("blockchain-size") & duration) { d =>
    ss.totalBlockChainSizeForDuration(d)
  }

  val avgTxsPerBlock = (get & pathPrefix("transactions-per-block") & duration) { d =>
    ss.avgTxsPerBlockForDuration(d)
  }

  val avgDifficulty = (get & pathPrefix("difficulty") & duration) { d =>
    ss.avgDifficultyForDuration(d)
  }

  val minerRevenue = (get & pathPrefix("miners-revenue") & duration) { d =>
    ss.minerRevenueForDuration(d)
  }

  val hashrate = (get & pathPrefix("hash-rate") & duration) { d =>
    ss.hashrateForDuration(d)
  }

  val hashrateDistribution = (get & pathPrefix("hash-rate-distribution")) {
    ss.sharesAcrossMinersFor24H
  }
}
