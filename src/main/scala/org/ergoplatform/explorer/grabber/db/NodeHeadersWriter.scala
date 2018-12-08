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

  type ToInsert = ApiHeader

  val fields = Seq(
    "id",
    "parent_id",
    "version",
    "height",
    "n_bits",
    "difficulty",
    "timestamp",
    "state_root",
    "ad_proofs_root",
    "transactions_root",
    "extension_hash",
    "pow_solutions",
    "interlinks",
    "main_chain"
  )

  implicit val MetaDifficulty: Meta[ApiDifficulty] = Meta[BigDecimal].xmap(
    x => ApiDifficulty(x.toBigInt()),
    x => BigDecimal.apply(x.value)
  )

  implicit val c: Composite[ApiHeader] = Composite[ApiHeader]

  val insertSql: String = s"INSERT INTO node_headers ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}
