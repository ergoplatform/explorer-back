package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ProtocolConfig
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.models.{BlockInfo, Header, MinerStats}
import org.ergoplatform.explorer.http.protocol._

import scala.concurrent.ExecutionContext
import scala.math.BigDecimal

trait StatsService[F[_]] {

  def findLastStats: F[StatsSummary]

  def findBlockchainInfo: F[BlockchainInfo]

  def totalCoinsForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgBlockSizeForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def totalBlockChainSizeForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgDifficultyForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgTxsPerBlockForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def sumTxsGroupByDayForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def minerRevenueForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def hashrateForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def sharesAcrossMinersFor24H: F[List[MinerStatSingleInfo]]

  def forksInfo(fromH: Long): F[ForksSummary]

}

class StatsServiceIOImpl[F[_]](protocolConfig: ProtocolConfig)(
  xa: Transactor[F],
  ec: ExecutionContext
)(implicit F: Monad[F], A: Async[F])
    extends StatsService[F] {

  private val emptyInfoResponse = BlockchainInfo("0.0.0", 0L, 0L, 0L)
  private val SecondsIn24H: Long = (24 * 60 * 60).toLong
  private val MillisIn24H: Long = SecondsIn24H * 1000L

  val infoDao = new BlockInfoDao
  val headersDao = new HeadersDao
  val outputsDao = new OutputsDao
  val tDao = new TransactionsDao
  val minerStatsDao = new MinerStatsDao

  override def forksInfo(fromH: Long): F[ForksSummary] = {
    @scala.annotation.tailrec
    def traceForks(heightSlices: List[List[Header]], acc: List[List[Header]]): List[List[Header]] =
      heightSlices match {
        case headSlice :: leftSlices =>
          val forkChains = headSlice.map { header =>
            leftSlices
              .foldLeft(Some(header): Option[Header], List(header)) {
                case ((Some(nextHeader), chainAcc), nextSlice) =>
                  val parentOpt = nextSlice.find(_.id == nextHeader.parentId)
                  parentOpt -> (chainAcc ++ parentOpt.toList)
                case ((_, chainAcc), _) =>
                  None -> chainAcc
              }
              ._2
          }
          traceForks(leftSlices, acc ++ forkChains)
        case Nil =>
          acc
      }

    infoDao.findLast
      .flatMap { bestBlockInfo =>
        val bestHeight = bestBlockInfo.map(_.height).getOrElse(0L)
        val minHeight = if (fromH < 0) bestHeight - Constants.EpochLength else fromH
        headersDao.getAtHeightRange(minHeight, bestHeight)
      }
      .transact[F](xa)
      .map { headers =>
        val (mainChain, nonMainChain) = headers.partition(_.mainChain)
        val slicedNonMainChain = nonMainChain.groupBy(_.height).values.toList.sortBy(-_.head.height)
        val forks = traceForks(slicedNonMainChain, List.empty).map { fork =>
          val branchPointOpt = mainChain
            .find { mainChainHeader =>
              fork.lastOption.exists(_.parentId == mainChainHeader.id)
            }
            .map(_.height)
          ForkInfo(fork.size, branchPointOpt, fork.reverse.map(h => h.height -> h.id))
        }
        ForksSummary(forks.size, forks)
      }
  }

  override def findLastStats: F[StatsSummary] =
    for {
      _                <- Async.shift[F](ec)
      pastTs           <- F.pure(System.currentTimeMillis() - MillisIn24H)
      totalOutputs     <- outputsDao.sumOfAllUnspentOutputsSince(pastTs).transact[F](xa)
      estimatedOutputs <- outputsDao.estimateOutputSince(pastTs).transact[F](xa)
      stats <- infoDao
        .findSince(pastTs)
        .transact[F](xa)
        .map(recentToStats(_, totalOutputs, estimatedOutputs))
    } yield stats

  private def percentOfFee(fees: Long, minersReward: Long) =
    if (fees + minersReward == 0L) {
      0.0
    } else {
      val result = fees.toDouble / (minersReward.toDouble + fees.toDouble)
      BigDecimal(result * 100).setScale(8, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

  private def percentOfTxVolume(minersReward: Long, totalCoins: Long): Double =
    if (totalCoins == 0L) {
      0.0
    } else {
      val result = minersReward.toDouble / totalCoins.toDouble
      BigDecimal(result * 100).setScale(8, BigDecimal.RoundingMode.HALF_UP).toDouble
    }

  private def recentToStats(
    blocks: List[BlockInfo],
    totalOutputs: Long,
    estimatedOutputs: BigDecimal
  ): StatsSummary =
    blocks.sortBy(info => -info.height) match {
      case Nil =>
        StatsSummary.empty
      case x :: _ =>
        val blocksCount = blocks.length.toLong
        val avgMiningTime = blocks.map(_.blockMiningTime).sum / blocksCount
        val coins = blocks.map(_.blockCoins).sum
        val txsCount = blocks.map(_.txsCount).sum
        val totalFee = blocks.map(_.blockFee).sum
        val minersRevenue = blocks.map(_.minerRevenue).sum
        val minersReward = blocks.map(_.minerReward).sum
        val hashrate = hashrateForSecs(blocks.map(_.difficulty).sum, SecondsIn24H)

        StatsSummary(
          blocksCount = blocksCount,
          blocksAvgTime = avgMiningTime,
          totalCoins = minersReward,
          totalTransactionsCount = txsCount,
          totalFee = totalFee,
          totalOutput = totalOutputs,
          estimatedOutput = estimatedOutputs.toBigInt(),
          totalMinerRevenue = minersRevenue,
          percentEarnedTransactionsFees = percentOfFee(totalFee, minersReward),
          percentTransactionVolume = percentOfTxVolume(minersReward, coins),
          costPerTx = if (txsCount == 0L) 0L else minersRevenue / txsCount,
          lastDifficulty = x.difficulty,
          totalHashrate = hashrate
        )
    }

  override def findBlockchainInfo: F[BlockchainInfo] =
    for {
      _      <- Async.shift[F](ec)
      result <- blockchainInfoResult
    } yield result.getOrElse(emptyInfoResponse)

  private def blockchainInfoResult: F[Option[BlockchainInfo]] =
    (for {
      header <- headersDao.getLast(1).map(_.headOption)
      pastTs = System.currentTimeMillis() - MillisIn24H
      difficulties <- infoDao.difficultiesSumSince(pastTs)
      hashrate = hashrateForSecs(difficulties, SecondsIn24H)
      txsCount <- tDao.countTxsSince(pastTs)
      info = header.map { h =>
        val supply = protocolConfig.emission.issuedCoinsAfterHeight(h.height)
        BlockchainInfo(h.version.toString, supply, txsCount, hashrate)
      }
    } yield info).transact[F](xa)

  override def totalCoinsForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.totalCoinsGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  override def avgBlockSizeForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.avgBlockSizeGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  override def totalBlockChainSizeForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.blockchainSizeGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  override def avgDifficultyForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.avgDifficultyGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  override def avgTxsPerBlockForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.avgTxsCountGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  def sumTxsGroupByDayForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.sumTxsCountGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  override def minerRevenueForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _      <- Async.shift[F](ec)
      result <- infoDao.minerRevenueGroupedByDay(d).map(pairsToChartData).transact[F](xa)
    } yield result

  override def hashrateForDuration(d: Int): F[List[ChartSingleData[Long]]] =
    for {
      _                 <- Async.shift[F](ec)
      difficultiesByDay <- infoDao.sumDifficultiesGroupedByDay(d).transact[F](xa)
      hashratesByDay = difficultiesByDay.map {
        case (ts, d, s) => (ts, hashrateForSecs(d, SecondsIn24H), s)
      }
      result = pairsToChartData(hashratesByDay)
    } yield result

  def sharesAcrossMinersFor24H: F[List[MinerStatSingleInfo]] =
    for {
      _      <- Async.shift[F](ec)
      result <- sharesAcrossMinersFor24HResult
    } yield result

  private def sharesAcrossMinersFor24HResult: F[List[MinerStatSingleInfo]] =
    for {
      rawStats <- minerStatsDao
        .minerStatsAfter(System.currentTimeMillis() - MillisIn24H)
        .transact[F](xa)
      stats <- F.pure(rawMinerStatsToView(rawStats))
    } yield stats

  private def rawMinerStatsToView(list: List[MinerStats]): List[MinerStatSingleInfo] = {
    val totalCount = list.map(_.blocksMined).sum
    def threshold(m: MinerStats): Boolean =
      ((m.blocksMined.toDouble * 100) / totalCount.toDouble) > 1.0

    val (big, other) = list.partition(threshold)
    val otherSumStats = MinerStatSingleInfo("other", other.map(_.blocksMined).sum)
    val bigOnes = big.map { info =>
      MinerStatSingleInfo(info.printableName, info.blocksMined)
    }

    (bigOnes :+ otherSumStats).sortBy(x => -x.value).filterNot(_.value == 0L)
  }

  private def pairsToChartData(list: List[(Long, Long, String)]): List[ChartSingleData[Long]] =
    list.map { case (ts, data, _) => ChartSingleData(ts, data) }

  private def hashrateForSecs(difficulty: Long, seconds: Long): Long = (difficulty / seconds) + 1L
}
