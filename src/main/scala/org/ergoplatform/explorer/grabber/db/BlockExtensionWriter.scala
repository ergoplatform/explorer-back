package org.ergoplatform.explorer.grabber.db

import doobie.util.composite.Composite
import org.ergoplatform.explorer.grabber.protocol.ApiBlockExtension

object BlockExtensionWriter extends BasicWriter {

  import org.ergoplatform.explorer.db.dao.BlockExtensionOps.{fields, tableName}

  override type ToInsert = ApiBlockExtension

  implicit val c: Composite[ApiBlockExtension] = Composite[ApiBlockExtension]

  val insertSql: String = s"INSERT INTO $tableName ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}
