package org.ergoplatform.explorer.db.dao

import doobie.ConnectionIO
import org.ergoplatform.explorer.db.models.BlockExtension

class BlockExtensionDao {

  def find(headerId: String): ConnectionIO[Option[BlockExtension]] = BlockExtensionOps.select(headerId).option

}
