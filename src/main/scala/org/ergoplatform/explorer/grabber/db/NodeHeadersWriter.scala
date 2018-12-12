package org.ergoplatform.explorer.grabber.db

import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.composite.Composite
import org.ergoplatform.explorer.grabber.protocol.{ApiDifficulty, ApiHeader}

object NodeHeadersWriter extends BasicWriter {

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
