package org.ergoplatform.explorer.grabber.db

import doobie._
import doobie.implicits._
import doobie.util.query.{Query, Query0}
import org.ergoplatform.explorer.db.models.BlockInfo

object BlockInfoWriter extends BasicWriter {

  type ToInsert = BlockInfo

  val fields = Seq(
    "header_id",
    "timestamp",
    "height",
    "difficulty",
    "block_size",
    "block_coins",
    "block_mining_time",
    "txs_count",
    "txs_size",
    "miner_name",
    "miner_address",
    "miner_reward",
    "miner_revenue",
    "block_fee",
    "block_chain_total_size",
    "total_txs_count",
    "total_coins_issued",
    "total_mining_time",
    "total_fees",
    "total_miners_reward",
    "total_coins_in_txs"
  )

  val insertFields = fields.mkString("(", ", ", ")")
  val holders = fields.map{ _ => "?" }.mkString("(", ", ", ")")
  val selectFR = Fragment.const(fields.mkString(", "))

  val insertSql = s"INSERT INTO blocks_info $insertFields VALUES $holders"

  def selectById(id: String): Query0[BlockInfo] =
    (fr"SELECT" ++ selectFR ++ fr"FROM blocks_info WHERE header_id = $id").query[BlockInfo]

  def get(id: String): ConnectionIO[BlockInfo] = selectById(id).unique

}
