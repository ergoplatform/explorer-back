package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor, Json}

case class ApiOutput(boxId: String, value: Long, proposition: String, additionalRegisters: Json)

object ApiOutput {

  implicit val decoder: Decoder[ApiOutput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    value <- c.downField("value").as[Long]
    proposition <- c.downField("proposition").as[String]
    additionalRegisters <- c.downField("additionalRegisters").as[Json]
  } yield ApiOutput(boxId, value, proposition, additionalRegisters)
}
