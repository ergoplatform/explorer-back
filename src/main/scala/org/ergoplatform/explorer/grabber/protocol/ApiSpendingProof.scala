package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._

final case class ApiSpendingProof(proofBytes: String, extension: Json)

object ApiSpendingProof {

  implicit val encoder: Encoder[ApiSpendingProof] = { obj =>
    Json.obj(
      "proofBytes" -> obj.proofBytes.asJson,
      "extension"  -> obj.extension
    )
  }

  implicit val decoder: Decoder[ApiSpendingProof] = { c: HCursor =>
    for {
      proofBytes <- c.downField("proofBytes").as[String]
      extension  <- c.downField("extension").as[Json]
    } yield ApiSpendingProof(proofBytes, extension)
  }

}
