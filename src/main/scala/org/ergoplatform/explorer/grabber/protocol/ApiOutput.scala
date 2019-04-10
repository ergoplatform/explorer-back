package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor, Json}

case class ApiOutput(boxId: String,
                     value: Long,
                     creationHeight: Int,
                     ergoTree: String,
                     assets: Json,
                     additionalRegisters: Json)

object ApiOutput {

  implicit val decoder: Decoder[ApiOutput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    value <- c.downField("value").as[Long]
    creationHeight <- c.downField("creationHeight").as[Int]
    ergoTree <- c.downField("ergoTree").as[String]
    assets <- c.downField("assets").as[Json]
    additionalRegisters <- c.downField("additionalRegisters").as[Json]
  } yield ApiOutput(boxId, value, creationHeight, ergoTree, assets, additionalRegisters)
}
