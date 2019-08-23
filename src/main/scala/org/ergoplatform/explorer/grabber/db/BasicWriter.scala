package org.ergoplatform.explorer.grabber.db

import cats.instances.list._
import doobie.util.Write
import doobie.{ConnectionIO, Update}

trait BasicWriter {

  type ToInsert

  val insertSql: String

  def insertOp(implicit c: Write[ToInsert]): Update[ToInsert] = Update[ToInsert](insertSql)

  def insert(one: ToInsert)(implicit c: Write[ToInsert]): ConnectionIO[Int] = insertOp.run(one)

  def insertMany(list: List[ToInsert])(implicit c: Write[ToInsert]): ConnectionIO[Int] =
    insertOp.updateMany(list)

}
