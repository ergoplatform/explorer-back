package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor}

case class ApiInput(boxId: String, spendingProof: ApiSpendingProof)

object ApiInput {

  implicit val decoder: Decoder[ApiInput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    spendingProof <- c.downField("spendingProof").as[ApiSpendingProof]
  } yield ApiInput(boxId, spendingProof)
}
