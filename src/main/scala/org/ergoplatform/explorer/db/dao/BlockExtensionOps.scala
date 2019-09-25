package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.BlockExtension

object BlockExtensionOps extends DaoOps with JsonMeta {

  val tableName: String = "node_extensions"

  val fields: Seq[String] = Seq(
    "header_id",
    "digest",
    "fields"
  )

  def select(id: String): Query0[BlockExtension] =
    (fr"SELECT" ++ fieldsFr ++ fr"FROM node_extensions WHERE header_id = $id;")
      .query[BlockExtension]

  def insert: Update[BlockExtension] = Update[BlockExtension](insertSql)

}
