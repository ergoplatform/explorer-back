package org.ergoplatform.explorer.db.dao

import doobie.ConnectionIO
import org.ergoplatform.explorer.db.models.RawSearchBlock

class BlockListDao {

  def count(startTs: Long, endTs: Long): ConnectionIO[Long] =
    BlockListOps.count(startTs, endTs).unique

  def list(
    offset: Int = 0,
    limit: Int = 20,
    sortBy: String = "height",
    sortOrder: String = "DESC",
    startTs: Long,
    endTs: Long
  ): ConnectionIO[List[RawSearchBlock]] =
    BlockListOps.list(offset, limit, sortBy, sortOrder, startTs, endTs).to[List]

  /** Search block related info by the fragment of the identifier */
  def searchById(substring: String): ConnectionIO[List[RawSearchBlock]] =
    BlockListOps.searchById(substring).to[List]

}
