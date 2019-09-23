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

}
