package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.StatsDao
import org.ergoplatform.explorer.db.models.StatRecord
import org.ergoplatform.explorer.http.protocol.{ChartSingleData, StatsSummary}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.Duration

trait StatsService[F[_]] {

  def findLastStats: F[Option[StatsSummary]]

  def totalCoinsForDuration(d: Duration): F[List[ChartSingleData]]

  def avgBlockSizeForDuration(d: Duration): F[List[ChartSingleData]]
}

class StatsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                        (implicit F: Monad[F], A: Async[F]) extends StatsService[F] {

  val statsDao = new StatsDao

  override def findLastStats: F[Option[StatsSummary]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findLast.map(statRecordToStatsSummary).transact[F](xa)
  } yield result

  override def totalCoinsForDuration(d: Duration): F[List[ChartSingleData]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findStatsByDuration(d).map(statsToTotalCoins).transact[F](xa)
  } yield result

  override def avgBlockSizeForDuration(d: Duration): F[List[ChartSingleData]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findStatsByDuration(d).map(statsToAvgBlockSize).transact[F](xa)
  } yield result


  private def statRecordToStatsSummary(s: Option[StatRecord]): Option[StatsSummary] = s.map(StatsSummary.apply)

  private def statsToTotalCoins(list: List[StatRecord]): List[ChartSingleData] =
    list.map(ChartSingleData.toTotalCoinsData)

  private def statsToAvgBlockSize(list: List[StatRecord]): List[ChartSingleData] =
    list.map(ChartSingleData.toAvgBlockSizeData)



}
