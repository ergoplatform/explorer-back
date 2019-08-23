package org.ergoplatform.explorer.grabber.db

import doobie.util.Write
import org.ergoplatform.explorer.db.dao.{DaoOps, HeadersOps}
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.grabber.protocol.ApiHeader

object HeaderWriter extends BasicWriter with JsonMeta {

  type ToInsert = ApiHeader

  implicit val w: Write[ApiHeader] = Write[ApiHeader]

  val ops: DaoOps = HeadersOps

}
