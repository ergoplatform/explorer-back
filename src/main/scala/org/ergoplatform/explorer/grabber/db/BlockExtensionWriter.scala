package org.ergoplatform.explorer.grabber.db

import doobie.util.composite.Composite
import org.ergoplatform.explorer.db.models.BlockExtension

object BlockExtensionWriter extends BasicWriter {

  import org.ergoplatform.explorer.db.dao.BlockExtensionOps.fields

  override type ToInsert = BlockExtension

  implicit val c: Composite[BlockExtension] = Composite[BlockExtension]

  val insertSql: String = s"INSERT INTO node_extensions ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}
