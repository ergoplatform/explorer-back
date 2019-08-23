package org.ergoplatform.explorer.db.dao

import doobie.util.fragment.Fragment

trait DaoOps {

	val tableName: String

	val fields: Seq[String]

	lazy val fieldsString: String = fields.mkString(", ")
	lazy val holdersString: String = fields.map(_ => "?").mkString(", ")
	lazy val updateString: String = fields.map(f => s"$f = ?").mkString(", ")
	lazy val insertSql = s"INSERT INTO $tableName ($fieldsString) VALUES ($holdersString)"
	lazy val updateByIdSql = s"UPDATE $tableName SET $updateString WHERE id = ?"

	lazy val fieldsFr: Fragment = Fragment.const(fieldsString)

}
