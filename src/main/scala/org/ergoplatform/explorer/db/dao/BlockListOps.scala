package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.RawSearchBlock

object BlockListOps {

  def count(sTs: Long, eTs: Long): Query0[Long] =
    fr"SELECT count(id) FROM node_headers WHERE (timestamp >= $sTs) AND (timestamp <= $eTs) AND main_chain = TRUE"
      .query[Long]

  def list(offset: Int = 0,
           limit: Int = 20,
           sortBy: String,
           sortOrder: String,
           startTs: Long,
           endTs: Long): Query0[RawSearchBlock] = {
    (fr"SELECT h.id, h.height, h.timestamp, i.txs_count, i.miner_address, i.miner_name, i.block_size" ++
      fr"FROM node_headers h JOIN blocks_info i ON h.id = i.header_id" ++
      fr"WHERE  ((timestamp >= $startTs) AND (timestamp <= $endTs) AND main_chain = TRUE)" ++
      Fragment.const("ORDER BY " + sortBy + " " + sortOrder) ++
      fr"LIMIT ${limit.toLong} OFFSET ${offset.toLong};").query[RawSearchBlock]
  }
}
