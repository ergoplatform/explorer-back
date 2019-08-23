package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.AdProof

final case class AdProofInfo(headerId: String, proofBytes: String, digest: String)

object AdProofInfo {

  def apply(adProof: AdProof): AdProofInfo =
    AdProofInfo(adProof.headerId, adProof.proofBytes, adProof.digest)

  implicit val encoder: Encoder[AdProofInfo] = { p: AdProofInfo =>
    Json.obj(
      "headerId"   -> Json.fromString(p.headerId),
      "proofBytes" -> Json.fromString(p.proofBytes),
      "digest"     -> Json.fromString(p.digest)
    )
  }

}
