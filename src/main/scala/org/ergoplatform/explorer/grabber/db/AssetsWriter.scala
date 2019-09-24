package org.ergoplatform.explorer.grabber.db
import org.ergoplatform.explorer.db.dao.{AssetsOps, DaoOps}

object AssetsWriter extends BasicWriter {

  type ToInsert = (String, String, Long)

  val ops: DaoOps = AssetsOps

}
