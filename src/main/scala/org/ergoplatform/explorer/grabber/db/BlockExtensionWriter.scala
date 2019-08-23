package org.ergoplatform.explorer.grabber.db

import doobie.util.Write
import org.ergoplatform.explorer.db.models.BlockExtension

object BlockExtensionWriter extends BasicWriter {

  import org.ergoplatform.explorer.db.dao.BlockExtensionOps.fields

  override type ToInsert = BlockExtension

  implicit val w: Write[BlockExtension] = Write[BlockExtension]

  val insertSql: String = s"INSERT INTO node_extensions ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}
