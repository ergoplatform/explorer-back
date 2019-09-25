package org.ergoplatform.explorer.grabber.db

import doobie._
import doobie.implicits._
import doobie.util.query.Query0
import org.ergoplatform.explorer.db.dao.{BlockInfoOps, DaoOps}
import org.ergoplatform.explorer.db.models.BlockInfo

object BlockInfoWriter extends BasicWriter {

  type ToInsert = BlockInfo

  val ops: DaoOps = BlockInfoOps

  val selectFR: Fragment = Fragment.const(BlockInfoOps.fields.mkString(", "))

  def selectById(id: String): Query0[BlockInfo] =
    (fr"SELECT" ++ selectFR ++ fr"FROM blocks_info WHERE header_id = $id").query[BlockInfo]

  def get(id: String): ConnectionIO[Option[BlockInfo]] = selectById(id).option

}
