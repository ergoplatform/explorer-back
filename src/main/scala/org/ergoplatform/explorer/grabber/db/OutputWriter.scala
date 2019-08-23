package org.ergoplatform.explorer.grabber.db

import io.circe.Json
import org.ergoplatform.explorer.db.dao.{DaoOps, OutputsOps}

object OutputWriter extends BasicWriter {

  type ToInsert = (String, String, Long, Int, Int, String, String, Json, Long)

  val ops: DaoOps = OutputsOps

}
