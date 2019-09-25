package org.ergoplatform.explorer.grabber.db

import doobie.util.Write
import org.ergoplatform.explorer.db.dao.{BlockExtensionOps, DaoOps}
import org.ergoplatform.explorer.db.models.BlockExtension

object BlockExtensionWriter extends BasicWriter {

  override type ToInsert = BlockExtension

  implicit val w: Write[BlockExtension] = Write[BlockExtension]

  val ops: DaoOps = BlockExtensionOps

}
