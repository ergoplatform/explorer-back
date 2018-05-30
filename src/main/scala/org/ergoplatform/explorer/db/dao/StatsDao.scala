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
}
