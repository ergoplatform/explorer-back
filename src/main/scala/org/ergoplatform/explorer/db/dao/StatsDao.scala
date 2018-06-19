package org.ergoplatform.explorer.db.dao

import cats.data._
import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.StatRecord

class StatsDao {

  val fields = StatsOps.fields

  def insert(s: StatRecord): ConnectionIO[StatRecord] =
    StatsOps.insert.withUniqueGeneratedKeys[StatRecord](fields: _*)(s)

  def insertMany(list: List[StatRecord]): ConnectionIO[List[StatRecord]] =
    StatsOps.insert.updateManyWithGeneratedKeys[StatRecord](fields: _*)(list).compile.to[List]

  def findLast: ConnectionIO[Option[StatRecord]] = StatsOps.findLast(1).option

  def totalCoinsGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] =
    StatsOps.totalCoinsGroupedByDay(lastDays).to[List]

  def avgBlockSizeGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] =
    StatsOps.avgBlockSizeGroupedByDay(lastDays).to[List]

  def avgTxsCountGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] =
    StatsOps.avgTxsGroupedByDay(lastDays).to[List]

  def blockchainSizeGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] =
    StatsOps.totalBlockchainSizeGroupedByDay(lastDays).to[List]

  def avgDifficultyGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] =
    StatsOps.avgDifficultyGroupedByDay(lastDays).to[List]

  def minerRevenueGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] =
    StatsOps.minerRevenueGroupedByDay(lastDays).to[List]

  def difficultiesSumSince(ts: Long): ConnectionIO[Long] = StatsOps.difficultiesSumSince(ts).unique

  def circulatingSupplySince(ts: Long): ConnectionIO[Long] = StatsOps.circulatingSupplySince(ts).unique

  def deleteAll: ConnectionIO[Unit] = StatsOps.deleteAll.toUpdate0(()).run.map { _ => () }
}
