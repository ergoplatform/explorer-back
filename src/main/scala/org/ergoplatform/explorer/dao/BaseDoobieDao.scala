package org.ergoplatform.explorer.dao

import doobie.free.connection.ConnectionIO
import org.ergoplatform.explorer.models.Entity
import doobie._
import doobie.implicits._
import cats.data._
import cats.implicits._

abstract class BaseDoobieDao[ID, E <: Entity[ID]] {

  def table: String

  def fields: Seq[String]

  def idFieldName = "id"

  def fieldsString: String = fields.mkString(", ")

  def placeHolders: String = fields.map(_ => "?").mkString(", ")

  def fromFr: Fragment = fr"FROM" ++ Fragment.const(table)

  def fieldsFr: Fragment = Fragment.const(fieldsString)

  def selectAllFromFr: Fragment = fr"SELECT" ++ fieldsFr ++ fromFr

  def limitFr(limit: Int): Fragment = fr"LIMIT" ++ Fragment.const(limit.toString)

  def offsetFr(offset: Int): Fragment = fr"OFFSET" ++ Fragment.const(offset.toString)

  def sortBy(field: String, order: String = "DESC"): Fragment =
    fr"ORDER BY" ++ Fragment.const(field) ++ Fragment.const(order)

  val updateSql = s"UPDATE $table SET ${fields.map(f => s"$f = ?").mkString(", ")} where $idFieldName = ?"
  val insertSql = s"INSERT INTO $table ($fieldsString) VALUES ($placeHolders)"

  def find(id: ID)(implicit i: Composite[ID], e: Composite[E]): ConnectionIO[Option[E]] = {
    val sql = s"SELECT $fieldsString FROM $table where $idFieldName = ?"
    HC.stream[E](sql, HPS.set(id), 1).take(1).compile.toList.map(_.headOption)
  }

  def get(id: ID)(implicit i: Composite[ID], e: Composite[E]): ConnectionIO[E] = {
    find(id).flatMap {
      case Some(x) =>
        x.pure[ConnectionIO]
      case None =>
        doobie.free.connection.raiseError(
          new NoSuchElementException(s"Cannot find element with id = $id in table $table")
        )
    }
  }



  def list(offset: Int = 0, limit: Int = 20)(implicit e: Composite[E]): ConnectionIO[List[E]] = {
    val sql = selectAllFromFr ++ limitFr(limit) ++ offsetFr(offset)
    sql.query[E].stream.compile.toList
  }

  def count: ConnectionIO[Long] = {
    (fr"SELECT COUNT(*)" ++ fromFr).query[Long].unique
  }

  //Make safe
  def insert(entity: E)(implicit e: Composite[E]): ConnectionIO[E] = {
    Update[E](insertSql).withGeneratedKeys[E](fields: _*)(entity).compile.toList.map(_.head)
  }

  def insertMany(list: List[E])(implicit e: Composite[E]): ConnectionIO[List[E]] = {
    Update[E](insertSql).updateManyWithGeneratedKeys[E](fields: _*)(list).compile.toList
  }

  //Make safe
  def updateById(id: ID, data: E)(implicit i: Composite[ID], e: Composite[E]): ConnectionIO[E] = {
    Update[(E, ID)](updateSql).withGeneratedKeys[E](fields: _*)(data -> id).compile.toList.map(_.head)
  }

  //Make safe
  def update(entity: E)(implicit i: Composite[ID], e: Composite[E]): ConnectionIO[E] = {
    Update[(E, ID)](updateSql).withGeneratedKeys[E](fields: _*)(entity -> entity.id).compile.toList.map(_.head)
  }

  def updateMany(list: List[E])(implicit i: Composite[ID], e: Composite[E]): ConnectionIO[List[E]] = {
    Update[(E, ID)](updateSql).updateManyWithGeneratedKeys[E](fields: _*)(list.map(e => e -> e.id)).compile.toList
  }
}
