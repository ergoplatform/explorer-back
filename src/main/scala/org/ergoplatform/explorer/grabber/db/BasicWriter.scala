package org.ergoplatform.explorer.grabber.db

import cats.instances.list._
import doobie.util.Write
import doobie.{ConnectionIO, Update}
import org.ergoplatform.explorer.db.dao.DaoOps

trait BasicWriter {

  type ToInsert

  val ops: DaoOps

  lazy val insertSql: String = s"INSERT INTO ${ops.tableName} ${ops.fields.mkString("(", ", ", ")")} " +
    s"VALUES ${ops.fields.map(_ => "?").mkString("(", ", ", ")")}"

  def insertOp(implicit c: Write[ToInsert]): Update[ToInsert] = Update[ToInsert](insertSql)

  def insert(one: ToInsert)(implicit c: Write[ToInsert]): ConnectionIO[Int] = insertOp.run(one)

  def insertMany(list: List[ToInsert])(implicit c: Write[ToInsert]): ConnectionIO[Int] =
    insertOp.updateMany(list)

}
