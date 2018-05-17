package org.ergoplatform.explorer.db.dao

import doobie._ ,doobie.implicits._
import cats.data._ ,cats.implicits._
import org.ergoplatform.explorer.db.models.Interlink

class InterlinksDao extends BaseDoobieDao[Long, Interlink] {

  override def table: String = "interlinks"
  override def fields: Seq[String] = Seq(
    "id",
    "modifier_id",
    "block_id"
  )

  val insertDataSql = s"INSERT INTO $table (modifier_id, block_id) VALUES (?, ?)"

  def insertData(t: (String, String))(implicit c: Composite[Interlink]): ConnectionIO[Interlink] = {
    Update[(String, String)](insertDataSql)
      .withGeneratedKeys[Interlink](fields: _*)(t)
      .compile
      .toList
      .map(_.head)
  }

  def insertManyData(list: List[(String, String)])(implicit c: Composite[Interlink]): ConnectionIO[List[Interlink]] = {
    Update[(String, String)](insertDataSql)
      .updateManyWithGeneratedKeys[Interlink](fields: _*)(list)
      .compile
      .toList
  }

  def findAllByBLockId(blockId: String)(implicit c: Composite[Interlink]): ConnectionIO[List[Interlink]] = {
    (selectAllFromFr ++ Fragment.const(s"WHERE block_id = '$blockId'")).query[Interlink].to[List]
  }
}
