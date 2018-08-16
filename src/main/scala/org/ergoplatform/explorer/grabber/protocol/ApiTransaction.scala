package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor}

case class ApiTransaction(id: String, inputs: List[ApiInput], outputs: List[ApiOutput], size: Long)

object ApiTransaction {

  implicit val decoder: Decoder[ApiTransaction] = (c: HCursor) => for {
    id <- c.downField("id").as[String]
    inputs <- c.downField("inputs").as[List[ApiInput]]
    outputs <- c.downField("outputs").as[List[ApiOutput]]
    size <- c.downField("size").as[Long]
  } yield ApiTransaction(id, inputs, outputs, size)
}
