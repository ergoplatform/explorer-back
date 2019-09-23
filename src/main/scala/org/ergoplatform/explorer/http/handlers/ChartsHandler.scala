package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.services.StatsService

final class ChartsHandler(ss: StatsService[IO]) extends RouteHandler {

  val route: Route = pathPrefix("charts") {
    totalCoins ~
    avgBlockSize ~
    blockChainSize ~
    avgTxsPerBlock ~
    sumTxsPerBlock ~
    avgDifficulty ~
    minerRevenue ~
    hashrateDistribution ~
    hashrate
  }

  def totalCoins: Route = (get & pathPrefix("total") & duration) { d =>
    ss.totalCoinsForDuration(d)
  }

  def avgBlockSize: Route = (get & pathPrefix("block-size") & duration) { d =>
    ss.avgBlockSizeForDuration(d)
  }

  def blockChainSize: Route = (get & pathPrefix("blockchain-size") & duration) { d =>
    ss.totalBlockChainSizeForDuration(d)
  }

  def avgTxsPerBlock: Route = (get & pathPrefix("transactions-per-block") & duration) { d =>
    ss.avgTxsPerBlockForDuration(d)
  }

  def sumTxsPerBlock: Route = (get & pathPrefix("transactions-number") & duration) { d =>
    ss.sumTxsGroupByDayForDuration(d)
  }

  def avgDifficulty: Route = (get & pathPrefix("difficulty") & duration) { d =>
    ss.avgDifficultyForDuration(d)
  }

  def minerRevenue: Route = (get & pathPrefix("miners-revenue") & duration) { d =>
    ss.minerRevenueForDuration(d)
  }

  def hashrate: Route = (get & pathPrefix("hash-rate") & duration) { d =>
    ss.hashrateForDuration(d)
  }

  def hashrateDistribution: Route = (get & pathPrefix("hash-rate-distribution")) {
    ss.sharesAcrossMinersFor24H
  }

}
