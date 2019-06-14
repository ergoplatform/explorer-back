package org.ergoplatform.explorer.grabber.db

import doobie._
import doobie.util.composite.Composite
import org.ergoplatform.explorer.grabber.protocol.{ApiDifficulty, ApiHeader}

object HeaderWriter extends BasicWriter {

  import org.ergoplatform.explorer.db.dao.HeadersOps.fields

  type ToInsert = ApiHeader

  implicit val MetaDifficulty: Meta[ApiDifficulty] = Meta[BigDecimal].xmap(
    x => ApiDifficulty(x.toBigInt()),
    x => BigDecimal.apply(x.value)
  )

  implicit val c: Composite[ApiHeader] = Composite[ApiHeader]

  val insertSql: String = s"INSERT INTO node_headers ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}
