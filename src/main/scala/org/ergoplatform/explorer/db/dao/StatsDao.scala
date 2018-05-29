package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.db.models.StatRecord

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
    "block_mining_time"
  )

  def findLast: ConnectionIO[Option[StatRecord]] = {
    list(0, 1, "ts", "DESC").map(_.headOption)
  }
}
