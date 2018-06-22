package org.ergoplatform.explorer.db.dao

import doobie._, doobie.implicits._, doobie.postgres.implicits._
import doobie.util.query.Query0
import org.ergoplatform.explorer.db.models.AdProof

object AdProofsOps {

  val fields = Seq(
    "header_id",
    "proof_bytes",
    "digest"
  )

  def select(headerId: String): Query0[AdProof] =
    fr"SELECT header_id, proof_bytes, diget FROM node_ad_proofs WHERE header_id = $headerId".query[AdProof]


}
