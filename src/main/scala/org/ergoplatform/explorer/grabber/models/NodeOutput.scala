package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor, Json}

case class NodeOutput(boxId: String, value: Long, proposition: String, additionalRegisters: Json)

object NodeOutput {

  implicit val decoder: Decoder[NodeOutput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    value <- c.downField("value").as[Long]
    proposition <- c.downField("proposition").as[String]
    additionalRegisters <- c.downField("additionalRegisters").as[Json]
  } yield NodeOutput(boxId, value, proposition, additionalRegisters)
}
