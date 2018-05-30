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
import scala.concurrent.duration.Duration

trait StatsService[F[_]] {

  def findLastStats: F[Option[StatsSummary]]

  def findBlockchainInfo: F[Option[BlockchainInfo]]

  def totalCoinsForDuration(d: Duration): F[List[ChartSingleData[Long]]]

  def avgBlockSizeForDuration(d: Duration): F[List[ChartSingleData[Long]]]

  def marketPriceUsdForDuration(d: Duration): F[List[ChartSingleData[UsdPriceInfo]]]
}

class StatsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                        (implicit F: Monad[F], A: Async[F]) extends StatsService[F] {

  val statsDao = new StatsDao

  override def findLastStats: F[Option[StatsSummary]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findLast.map(statRecordToStatsSummary).transact[F](xa)
  } yield result

  override def findBlockchainInfo: F[Option[BlockchainInfo]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findLast.map(statRecordToBlockchainInfo).transact[F](xa)
  } yield result

  override def totalCoinsForDuration(d: Duration): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findStatsByDuration(d).map(statsToTotalCoins).transact[F](xa)
  } yield result

  override def avgBlockSizeForDuration(d: Duration): F[List[ChartSingleData[Long]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findStatsByDuration(d).map(statsToAvgBlockSize).transact[F](xa)
  } yield result

  def marketPriceUsdForDuration(d: Duration): F[List[ChartSingleData[UsdPriceInfo]]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findStatsByDuration(d).map(statsToUsdPriceInfo).transact[F](xa)
  } yield result


  private def statRecordToStatsSummary(s: Option[StatRecord]): Option[StatsSummary] = s.map(StatsSummary.apply)

  private def statRecordToBlockchainInfo(s: Option[StatRecord]): Option[BlockchainInfo] = s.map(BlockchainInfo.apply)

  private def statsToTotalCoins(list: List[StatRecord]): List[ChartSingleData[Long]] =
    list.map(ChartSingleData.toTotalCoinsData)

  private def statsToAvgBlockSize(list: List[StatRecord]): List[ChartSingleData[Long]] =
    list.map(ChartSingleData.toAvgBlockSizeData)

  private def statsToUsdPriceInfo(list: List[StatRecord]): List[ChartSingleData[UsdPriceInfo]] =
    list.map(ChartSingleData.toMarketPrice)
}
