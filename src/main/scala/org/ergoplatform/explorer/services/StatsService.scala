package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.{BlockInfoDao, HeadersDao, MinerStatsDao, OutputsDao}
import org.ergoplatform.explorer.db.models.{BlockInfo, MinerStats}
import org.ergoplatform.explorer.http.protocol._

import scala.concurrent.ExecutionContext

trait StatsService[F[_]] {

  def findLastStats: F[StatsSummary]

  def findBlockchainInfo: F[BlockchainInfo]

  def totalCoinsForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgBlockSizeForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgBlockChainSizeForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgDifficultyForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def avgTxsPerBlockForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def minerRevenueForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def hashrateForDuration(daysBack: Int): F[List[ChartSingleData[Long]]]

  def sharesAcrossMinersFor24H: F[List[MinerStatSingleInfo]]

}

class StatsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                        (implicit F: Monad[F], A: Async[F]) extends StatsService[F] {

  val emptyStatsResponse = StatsSummary.empty
  val emptyInfoResponse = BlockchainInfo("0.0.0", 0L, 0L, 0L)
  private val SecondsIn24H: Long = (24*60*60).toLong
  private val MillisIn24H: Long = SecondsIn24H * 1000L

  val infoDao = new BlockInfoDao
  val hDao = new HeadersDao
  val outputsDao = new OutputsDao
  val minerStatsDao = new MinerStatsDao

  override def findLastStats: F[StatsSummary] = for {
    _ <- Async.shift[F](ec)
    totalUnspentOutputsValue <- outputsDao.sumOfAllUnspentOutputs.transact[F](xa)
    totalDifficulties <- infoDao.difficultiesSumSince(0L).transact[F](xa)
    estimatedOutput <- outputsDao.estimateOutput.transact[F](xa)
    result <- infoDao
      .findLast
      .map(statRecordToStatsSummary(_, totalUnspentOutputsValue, totalDifficulties, estimatedOutput))
      .transact[F](xa)
  } yield result.getOrElse(emptyStatsResponse)

  override def findBlockchainInfo: F[BlockchainInfo] = {
    for {
      _ <- Async.shift[F](ec)
      result <- blockchainInfoResult
    } yield result.getOrElse(emptyInfoResponse)
  }

  private def blockchainInfoResult: F[Option[BlockchainInfo]] = (for {
    blockInfoRecord <- infoDao.findLast
    pastTs = System.currentTimeMillis() - MillisIn24H
    difficulties <- infoDao.difficultiesSumSince(pastTs)
    hashrate = hashratePerDay(difficulties)
    supply <- infoDao.circulatingSupplySince(pastTs)
    h <- hDao.get(blockInfoRecord.map(_.headerId).getOrElse(""))
    info = blockInfoRecord.map { v => BlockchainInfo(h.version.toString, supply, v.avgTxsCount, hashrate)}
  } yield info).transact[F](xa)

  override def totalCoinsForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- infoDao.totalCoinsGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgBlockSizeForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- infoDao.avgBlockSizeGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgBlockChainSizeForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- infoDao.blockchainSizeGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgDifficultyForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- infoDao.avgDifficultyGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgTxsPerBlockForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- infoDao.avgTxsCountGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def minerRevenueForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- infoDao.minerRevenueGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def hashrateForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    difficultiesByDay <- infoDao.sumDifficultiesGroupedByDay(d).transact[F](xa)
    hashratesByDay = difficultiesByDay.map { case (ts, d, s) => (ts, hashratePerDay(d), s)}
    result = pairsToChartData(hashratesByDay)
  } yield result

  def sharesAcrossMinersFor24H: F[List[MinerStatSingleInfo]] = for {
    _ <- Async.shift[F](ec)
    result <- sharesAcrossMinersFor24HResult
  } yield result

  private def sharesAcrossMinersFor24HResult: F[List[MinerStatSingleInfo]] = for {
    rawStats <- minerStatsDao.minerStatsAfter(/* System.currentTimeMillis() - MillisIn24H */ 0L).transact[F](xa)
    stats <- F.pure(rawMinerStatsToView(rawStats))
  } yield stats

  private def rawMinerStatsToView(list: List[MinerStats]): List[MinerStatSingleInfo] = {
    val totalCount = list.map(_.blocksMined).sum
    def threshold(m: MinerStats): Boolean = ((m.blocksMined.toDouble * 100) / totalCount.toDouble) > 1.0

    val (big, other) = list.partition(threshold)
    val otherSumStats = MinerStatSingleInfo("other", other.map(_.blocksMined).sum)
    val bigOnes = big.map { info =>
      MinerStatSingleInfo(info.printableName, info.blocksMined)
    }

    (bigOnes :+ otherSumStats).sortBy(v => -v.value).filterNot(_.value == 0L)
  }

  private def hashRate24H: F[Long] = for {
    difficulties <- infoDao.difficultiesSumSince(System.currentTimeMillis() - MillisIn24H).transact[F](xa)
    hashrate = difficulties / SecondsIn24H
  } yield hashrate

  private def statRecordToStatsSummary(s: Option[BlockInfo], to: Long, td: Long, eo: Long): Option[StatsSummary] =
    s.map(StatsSummary.apply(_, to, td, eo))

  private def pairsToChartData(list: List[(Long, Long, String)]): List[ChartSingleData[Long]] =
    list.map{ case (ts, data, _) => ChartSingleData(ts, data)}

  private def hashratePerDay(difficulty: Long): Long = difficulty / SecondsIn24H
}
