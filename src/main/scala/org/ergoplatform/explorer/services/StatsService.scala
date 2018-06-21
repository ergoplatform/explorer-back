package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.StatsDao
import org.ergoplatform.explorer.db.models.StatRecord
import org.ergoplatform.explorer.http.protocol.{BlockchainInfo, ChartSingleData, StatsSummary, UsdPriceInfo}

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

}

class StatsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                        (implicit F: Monad[F], A: Async[F]) extends StatsService[F] {

  val emptyStatsResponse = StatsSummary(StatRecord())
  val emptyInfoResponse = BlockchainInfo("0.0.0", 0L, 0L, 0L)
  private val SecondsIn24H: Long = (24*60*60).toLong
  private val MillisIn24H: Long = SecondsIn24H * 1000L

  val statsDao = new StatsDao

  override def findLastStats: F[StatsSummary] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findLast.map(statRecordToStatsSummary).transact[F](xa)
  } yield result.getOrElse(emptyStatsResponse)

  override def findBlockchainInfo: F[BlockchainInfo] = {
    for {
      _ <- Async.shift[F](ec)
      result <- blockchainInfoResult
    } yield result.getOrElse(emptyInfoResponse)
  }

  private def blockchainInfoResult: F[Option[BlockchainInfo]] = (for {
    statsRecord <- statsDao.findLast
    pastTs = System.currentTimeMillis() - MillisIn24H
    difficulties <- statsDao.difficultiesSumSince(pastTs)
    hashrate = hashratePerDay(difficulties)
    supply <- statsDao.circulatingSupplySince(pastTs)
    info = statsRecord.map { v => BlockchainInfo(v.version, supply, v.avgTxsCount, hashrate)}
  } yield info).transact[F](xa)

  override def totalCoinsForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.totalCoinsGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgBlockSizeForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.avgBlockSizeGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgBlockChainSizeForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.blockchainSizeGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgDifficultyForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.avgDifficultyGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def avgTxsPerBlockForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.avgTxsCountGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def minerRevenueForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.minerRevenueGroupedByDay(d).map(pairsToChartData).transact[F](xa)
  } yield result

  override def hashrateForDuration(d: Int): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    difficultiesByDay <- statsDao.sumDifficultiesGroupedByDay(d).transact[F](xa)
    hashratesByDay = difficultiesByDay.map { case (ts, d) => ts -> hashratePerDay(d)}
    result = pairsToChartData(hashratesByDay)
  } yield result

  private def hashRate24H: F[Long] = for {
    difficulties <- statsDao.difficultiesSumSince(System.currentTimeMillis() - MillisIn24H).transact[F](xa)
    hashrate = difficulties / SecondsIn24H
  } yield hashrate

  private def statRecordToStatsSummary(s: Option[StatRecord]): Option[StatsSummary] = s.map(StatsSummary.apply)

  private def pairsToChartData(list: List[(Long, Long)]): List[ChartSingleData[Long]] =
    list.map{ case (ts, data) => ChartSingleData(ts, data)}



  private def hashratePerDay(difficulty: Long): Long = difficulty / SecondsIn24H
}
