package org.ergoplatform.explorer.db.dao

import doobie._
import doobie.implicits._
import org.ergoplatform.explorer.db.models.Asset

object AssetsOps extends DaoOps {

  val tableName: String = "node_assets"

  val fields: Seq[String] = Seq(
    "id",
    "box_id",
    "value"
  )

  def insert: Update[Asset] = Update[Asset](insertSql)

  def getByBoxId(boxId: String): Query0[Asset] =
    (selectAllFr ++ fr"WHERE box_id = $boxId").query

  def holderAddresses(assetId: String, offset: Int, limit: Int): Query0[String] =
    (fr"SELECT o.address FROM" ++ tableNameFr ++ fr"a" ++
    fr"LEFT JOIN" ++ OutputsOps.tableNameFr ++ fr"o ON a.box_id = o.box_id" ++
    fr"LEFT JOIN" ++ InputsOps.tableNameFr ++ fr"i ON o.box_id = i.box_id" ++
    fr"LEFT JOIN" ++ TransactionsOps.tableNameFr ++ fr"t_in ON o.tx_id = t_in.id" ++
    fr"LEFT JOIN" ++ HeadersOps.tableNameFr ++ fr"h_in ON h_in.id = t_in.header_id" ++
    fr"LEFT JOIN" ++ TransactionsOps.tableNameFr ++ fr"t ON i.tx_id = t.id" ++
    fr"LEFT JOIN" ++ HeadersOps.tableNameFr ++ fr"h ON h.id = t.header_id" ++
    fr"WHERE h_in.main_chain = TRUE AND (i.box_id IS NULL OR h.main_chain = FALSE) AND a.id = $assetId" ++
    fr"OFFSET ${offset.toLong} LIMIT ${limit.toLong}")
      .query[String]

}
