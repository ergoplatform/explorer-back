package org.ergoplatform.explorer.grabber.db

import doobie.util.Write
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.grabber.protocol.ApiHeader

object HeaderWriter extends BasicWriter with JsonMeta {

  import org.ergoplatform.explorer.db.dao.HeadersOps.fields

  type ToInsert = ApiHeader

  implicit val w: Write[ApiHeader] = Write[ApiHeader]

  val insertSql: String = s"INSERT INTO node_headers ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}
