package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor, Json}

case class NodeSpendingProof(proofBytes: String, extension: Json)

object NodeSpendingProof {

  implicit val decoder: Decoder[NodeSpendingProof] = (c: HCursor) => for {
    proofBytes <- c.downField("proofBytes").as[String]
    extension <- c.downField("extension").as[Json]
  } yield NodeSpendingProof(proofBytes, extension)
}
