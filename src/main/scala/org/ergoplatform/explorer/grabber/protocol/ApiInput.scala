package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._

case class ApiInput(boxId: String, spendingProof: ApiSpendingProof)

object ApiInput {

  implicit val encoder: Encoder[ApiInput] = { obj =>
    Json.obj(
      "boxId" -> obj.boxId.asJson,
      "spendingProof" -> obj.spendingProof.asJson
    )
  }

  implicit val decoder: Decoder[ApiInput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    spendingProof <- c.downField("spendingProof").as[ApiSpendingProof]
  } yield ApiInput(boxId, spendingProof)

}
