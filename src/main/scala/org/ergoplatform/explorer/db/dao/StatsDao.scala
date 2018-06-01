package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import doobie.util.fragment.Fragment
import org.ergoplatform.explorer.db.models.StatRecord

import scala.concurrent.duration.{Duration, FiniteDuration}

class StatsDao extends BaseDoobieDao[Long, StatRecord] {

  override def table: String = "blockchain_stats"

  override def fields: Seq[String] = Seq(
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
    "supply",
    "market_cap",
    "hashrate",
    "market_price_usd",
    "market_price_btc"
  )

  def findLast: ConnectionIO[Option[StatRecord]] = {
    list(0, 1, "ts", "DESC").map(_.headOption)
  }

  def findStatsByDuration(d: Duration): ConnectionIO[List[StatRecord]] = {
    val duration = d match {
      case x: FiniteDuration => x.toMillis
      case _ => System.currentTimeMillis - java.time.LocalDate.of(2017, 12, 31).toEpochDay
    }
    val pastPoint = System.currentTimeMillis() - duration
    val sql = selectAllFromFr ++ Fragment.const(s"WHERE ts >= $pastPoint") ++ sortByFr("ts", "ASC")
    sql.query[StatRecord].to[List]
  }

  def totalCoinsGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] = {
    val selectStr = "min(ts) as t, max(total_coins)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def avgBlockSizeGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] = {
    val selectStr = "min(ts) as t, avg(block_size)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  def marketPriceGroupedByDay(lastDays: Int): ConnectionIO[List[(Long, Long)]] = {
    val selectStr = "min(ts) as t, last_value(market_price_usd)"
    groupedByDayStatsPair(lastDays, selectStr)
  }

  private def groupedByDayStatsPair(d: Int, selectStr: String): ConnectionIO[List[(Long, Long)]] = {
    val sql = selectByDay(d, selectStr)
    sql.query[(Long, Long)].to[List]
  }

  private def selectByDay(limitDaysBack: Int, selectStr: String): Fragment = {
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
}
