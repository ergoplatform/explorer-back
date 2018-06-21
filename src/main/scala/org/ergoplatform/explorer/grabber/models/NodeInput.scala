package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor}

case class NodeInput(boxId: String, spendingProof: NodeSpendingProof)

object NodeInput {

  implicit val decoder: Decoder[NodeInput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    spendingProof <- c.downField("spendingProof").as[NodeSpendingProof]
  } yield NodeInput(boxId, spendingProof)
}
