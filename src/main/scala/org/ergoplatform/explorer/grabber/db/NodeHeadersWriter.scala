package org.ergoplatform.explorer.grabber.db

import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import org.ergoplatform.explorer.grabber.models.{NodeDifficulty, NodeHeader}

object NodeHeadersWriter extends BasicWriter {

  type ToInsert = NodeHeader

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
    "equihash_solutions",
    "interlinks"
  )

  implicit val MetaDifficulty: Meta[NodeDifficulty] = Meta[BigDecimal].xmap(
    x => NodeDifficulty(x.toBigInt()),
    x => BigDecimal.apply(x.value)
  )

  implicit val c = Composite[NodeHeader]

  val insertSql = s"INSERT INTO node_headers ${fields.mkString("(", ", ", ")")} " +
    s"VALUES ${fields.map(_ => "?").mkString("(", ", ", ")")}"

}

