package org.ergoplatform.explorer.db.dao

import doobie._
import org.ergoplatform.explorer.db.models.AdProof

class AdProofsDao {

  def find(headerId: String): ConnectionIO[Option[AdProof]] = AdProofsOps.select(headerId).option

}
