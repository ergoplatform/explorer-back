package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor}

case class NodeTransaction(id: String, inputs: List[NodeInput], outputs: List[NodeOutput])

object NodeTransaction {

  implicit val decoder: Decoder[NodeTransaction] = (c: HCursor) => for {
    id <- c.downField("id").as[String]
    inputs <- c.downField("inputs").as[List[NodeInput]]
    outputs <- c.downField("outputs").as[List[NodeOutput]]
  } yield NodeTransaction(id, inputs, outputs)
}
