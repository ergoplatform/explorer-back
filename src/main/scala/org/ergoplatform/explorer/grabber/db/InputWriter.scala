package org.ergoplatform.explorer.grabber.db

import io.circe.Json
import org.ergoplatform.explorer.db.dao.{DaoOps, InputsOps}

object InputWriter extends BasicWriter {

  type ToInsert = (String, String, String, Json)

  val ops: DaoOps = InputsOps

}
