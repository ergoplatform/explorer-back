package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor, Json}

case class ApiSpendingProof(proofBytes: String, extension: Json)

object ApiSpendingProof {

  implicit val decoder: Decoder[ApiSpendingProof] = (c: HCursor) => for {
    proofBytes <- c.downField("proofBytes").as[String]
    extension <- c.downField("extension").as[Json]
  } yield ApiSpendingProof(proofBytes, extension)
}
