package org.ergoplatform.explorer.db.dao

import cats.data.NonEmptyList
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.query.Query0
import org.ergoplatform.explorer.db.models.BlockInfo

object BlockInfoOps {

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
    "total_miner_revenue"
  )

  val fieldsString = fields.mkString(", ")
  val holdersString = fields.map(_ => "?").mkString(", ")

  val fieldsFr = Fragment.const(fieldsString)

  def select(headerId: String): Query0[BlockInfo] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM blocks_info WHERE header_id = $headerId").query[BlockInfo]

  def select(headerIds: List[String]): Query0[BlockInfo] =
    (fr"SELECT" ++ fieldsFr ++ fr"WHERE" ++
      Fragments.in(fr"headers_id", NonEmptyList.fromListUnsafe(headerIds))).query[BlockInfo]

}
