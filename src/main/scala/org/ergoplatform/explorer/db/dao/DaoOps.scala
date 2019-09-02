package org.ergoplatform.explorer.db.dao

import doobie.Fragment
import doobie.implicits._

trait DaoOps {

	val tableName: String

	val fields: Seq[String]

	lazy val fieldsString: String = fields.mkString(", ")
	lazy val holdersString: String = fields.map(_ => "?").mkString(", ")
	lazy val updateString: String = fields.map(f => s"$f = ?").mkString(", ")
	lazy val insertSql = s"INSERT INTO $tableName ($fieldsString) VALUES ($holdersString)"
	lazy val updateByIdSql = s"UPDATE $tableName SET $updateString WHERE id = ?"

	lazy val tableNameFr: Fragment = Fragment.const(tableName)
	lazy val fieldsFr: Fragment = Fragment.const(fieldsString)

	lazy val selectAllFr : Fragment = fr"SELECT" ++ fieldsFr ++ fr"FROM" ++ tableNameFr

	def allFieldsRefFr(ref: String): Fragment =
		Fragment.const(fields.map(field => s"$ref.$field").mkString(", "))

}
