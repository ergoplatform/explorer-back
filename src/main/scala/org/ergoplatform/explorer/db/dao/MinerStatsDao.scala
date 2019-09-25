package org.ergoplatform.explorer.db.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.db.models.composite.MinerStats

class MinerStatsDao {

  def minerStatsAfter(ts: Long): ConnectionIO[List[MinerStats]] =
    MinerStatsOps.minerStatsAfter(ts).to[List]

}
