package org.ergoplatform.explorer.db.dao

import doobie.Fragment
import doobie.implicits._
import doobie.util.composite.Composite
import doobie.util.query.Query0
import org.ergoplatform.explorer.db.models.BlockExtension

object BlockExtensionOps {

  implicit val c: Composite[BlockExtension] = Composite[BlockExtension]

  val tableName: String = "node_extensions"

  val fields: Seq[String] = Seq(
    "header_id",
    "digest",
    "fields"
  )

  val fieldsFr: Fragment = Fragment.const(fields.mkString(", "))

  def select(headerId: String): Query0[BlockExtension] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM $tableName WHERE header_id = $headerId").query[BlockExtension]

}
