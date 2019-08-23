package org.ergoplatform.explorer.db.dao

object AssetsOps extends DaoOps {

	val tableName: String = "node_assets"

	val fields: Seq[String] = Seq(
		"id",
		"box_id",
		"value"
	)

}
