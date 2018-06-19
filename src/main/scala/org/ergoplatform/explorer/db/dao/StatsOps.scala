package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import doobie.util.fragment.Fragment
import org.ergoplatform.explorer.db.models.StatRecord

object StatsOps {

  val fields = Seq(
    "id",
    "ts",
    "block_size",
    "total_size",
    "txs_count",
    "txs_total_count",
    "blocks_count",
    "difficulty",
    "block_coins",
    "total_coins",
    "block_value",
    "block_fee",
    "total_mining_time",
    "block_mining_time",
    "version",
    "height",
    "total_coins_issued",
    "miner_revenue"
  )

  val fieldsFr = Fragment.const(fields.mkString(", "))

  val insertSql =
    s"INSERT INTO blockchain_stats (${fields.mkString(", ")}) VALUES (${fields.map(_ => "?").mkString(", ")})"

  def findLast(cnt: Int = 10): Query0[StatRecord] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM blockchain_stats ORDER BY ts DESC LIMIT ${cnt.toLong}").query[StatRecord]

  def difficultiesSumSince(ts: Long): Query0[Long] = {
    fr"SELECT COALESCE(CAST(SUM(difficulty) as BIGINT), 0) FROM blockchain_stats WHERE ts >= $ts".query[Long]
  }

  def circulatingSupplySince(ts: Long): Query0[Long] = {
    (fr"SELECT COALESCE(CAST(SUM(o.value) as BIGINT), 0) " ++
      fr"FROM transactions t RIGHT JOIN outputs o ON t.id = o.tx_id WHERE t.ts >= $ts").query[Long]
  }

  def totalCoinsGroupedByDay(lastDays: Int): Query0[(Long, Long)] = {
    val selectStr = "min(ts) as t, max(total_coins)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgBlockSizeGroupedByDay(lastDays: Int): Query0[(Long, Long)] = {
    val selectStr = "min(ts) as t, avg(block_size)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgTxsGroupedByDay(lastDays: Int): Query0[(Long, Long)] = {
    val selectStr = "min(ts) as t, avg(txs_count)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def totalBlockchainSizeGroupedByDay(lastDays: Int): Query0[(Long, Long)] = {
    val selectStr = "min(ts) as t, sum(block_size)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgDifficultyGroupedByDay(lastDays: Int): Query0[(Long, Long)] = {
    val selectStr = "min(ts) as t, avg(difficulty)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def minerRevenueGroupedByDay(lastDays: Int): Query0[(Long, Long)] = {
    val selectStr = "min(ts) as t, sum(miner_revenue)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def groupedByDayStatsPair(d: Int, selectStr: String): Query0[(Long, Long)] = {
    val sql = selectByDay(d, selectStr)
    sql.query[(Long, Long)]
  }

  def selectByDay(limitDaysBack: Int, selectStr: String): Fragment = {
    import scala.concurrent.duration._

    val whereFragment = if (limitDaysBack <=0 ) {
      Fragment.empty
    } else {
      val ms = System.currentTimeMillis - limitDaysBack.days.toMillis
      Fragment.const(s"WHERE ts >= $ms")
    }

    Fragment.const(
      s"SELECT $selectStr, TO_CHAR(TO_TIMESTAMP(ts / 1000), 'DD/MM/YYYY') as date " +
        s"FROM blockchain_stats") ++ whereFragment ++ Fragment.const("GROUP BY date ORDER BY t ASC")
  }

  def insert: Update[StatRecord] = Update[StatRecord](insertSql)

  def deleteAll: Update[Unit] = Update[Unit]("DELETE FROM blockchain_stats")

}
