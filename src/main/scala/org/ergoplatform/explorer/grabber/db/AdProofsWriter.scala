package org.ergoplatform.explorer.grabber.db
import org.ergoplatform.explorer.db.dao.{AdProofsOps, DaoOps}

object AdProofsWriter extends BasicWriter {

  type ToInsert = (String, String, String)

  val ops: DaoOps = AdProofsOps

}
