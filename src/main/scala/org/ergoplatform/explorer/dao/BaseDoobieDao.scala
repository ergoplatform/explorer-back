package org.ergoplatform.explorer.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.models.Entity
import doobie._
import doobie.implicits._

abstract class BaseDoobieDao[ID, E <: Entity[ID]] {

  def table: String
  def fields: Seq[String]
  def fieldsString: String = fields.map { f => s"$table.$f" }.mkString(", ")

  def find(id: ID)(implicit idc: Composite[ID], ec: Composite[E]): ConnectionIO[Option[E]] = {
    val sql = s"SELECT $fieldsString FROM $table where id = ?"
    HC.stream[E](sql, HPS.set(id), 2).take(1).compile.toList.map(_.headOption)
  }
}
