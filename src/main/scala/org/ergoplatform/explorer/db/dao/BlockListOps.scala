package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.models.composite.RawSearchBlock

object BlockListOps {

  def count(sTs: Long, eTs: Long): Query0[Long] =
    fr"SELECT count(id) FROM node_headers WHERE (timestamp >= $sTs) AND (timestamp <= $eTs) AND main_chain = TRUE"
      .query[Long]

  def list(
    offset: Int = 0,
    limit: Int = 20,
    sortBy: String,
    sortOrder: String,
    startTs: Long,
    endTs: Long
  ): Query0[RawSearchBlock] = {
    val sortByValue = sortBy match {
      case "height"       => "h.height"
      case "timestamp"    => "h.timestamp"
      case "txs_count"    => "i.txs_count"
      case "miner_name"   => "m.miner_name"
      case "block_size"   => "i.block_size"
      case "difficulty"   => "h.difficulty"
      case "miner_reward" => "i.miner_reward"
      case _              => "h.height"
    }

    (fr"SELECT h.id, h.height, h.timestamp, i.txs_count, i.miner_address, m.miner_name, i.block_size, h.difficulty, i.miner_reward" ++
    fr"FROM node_headers h JOIN blocks_info i ON h.id = i.header_id " ++
    fr"LEFT JOIN known_miners m ON i.miner_address = m.miner_address" ++
    fr"WHERE  ((h.timestamp >= $startTs) AND (h.timestamp <= $endTs) AND h.main_chain = TRUE)" ++
    Fragment.const("ORDER BY " + sortByValue + " " + sortOrder) ++
    fr"LIMIT ${limit.toLong} OFFSET ${offset.toLong};").query[RawSearchBlock]
  }

  def searchById(substring: String): Query0[RawSearchBlock] =
    (
      fr"SELECT h.id, h.height, h.timestamp, i.txs_count, i.miner_address, m.miner_name, i.block_size, h.difficulty, i.miner_reward" ++
      fr"FROM node_headers h JOIN blocks_info i ON h.id = i.header_id " ++
      fr"LEFT JOIN known_miners m ON i.miner_address = m.miner_address" ++
      fr"WHERE h.id LIKE ${"%" + substring + "%"}"
    ).query[RawSearchBlock]
}
