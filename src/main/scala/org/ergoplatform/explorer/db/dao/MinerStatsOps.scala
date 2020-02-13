package org.ergoplatform.explorer.db.dao

import doobie.implicits._
import doobie.util.query.Query0
import org.ergoplatform.explorer.db.models.composite.MinerStats

object MinerStatsOps {

  def minerStatsAfter(ts: Long): Query0[MinerStats] =
    fr"""
        SELECT bi.miner_address, COALESCE(CAST(SUM(bi.difficulty) as BIGINT), 0),
        COALESCE(CAST(SUM(bi.block_mining_time) as BIGINT), 0), COUNT(*) as count, m.miner_name
        FROM blocks_info_replica bi LEFT JOIN known_miners m ON (bi.miner_address = m.miner_address)
        WHERE timestamp >= $ts
        GROUP BY bi.miner_address, m.miner_name
        ORDER BY count DESC;
      """.query[MinerStats]
}
