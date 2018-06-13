package org.ergoplatform.explorer.db.dao

import doobie._, doobie.implicits._, doobie.postgres.implicits._
import org.ergoplatform.explorer.db.models.Interlink

object InterlinksOps {

  val fields: Seq[String] = Seq(
    "id",
    "modifier_id",
    "block_id"
  )

  val fieldsFr: Fragment = Fragment.const("id, modifier_id, block_id")

  val insertSql = s"INSERT INTO interlinks (modifier_id, block_id) VALUES (?, ?)"

  def insertTupled(implicit c: Composite[(String, String)]): Update[(String, String)] =
    Update[(String, String)](insertSql)

  def findAllByBlockId(blockId: String)(implicit c: Composite[Interlink]): Query0[Interlink] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM interlinks WHERE block_id = $blockId").query[Interlink]

}
