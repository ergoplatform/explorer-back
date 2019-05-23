package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._

case class ApiOutput(boxId: String,
                     value: Long,
                     creationHeight: Int,
                     ergoTree: String,
                     assets: List[ApiAsset],
                     additionalRegisters: Json)

object ApiOutput {

  implicit val encoder: Encoder[ApiOutput] = { obj =>
    Json.obj(
      "boxId" -> obj.boxId.asJson,
      "value" -> obj.value.asJson,
      "creationHeight" -> obj.creationHeight.asJson,
      "ergoTree" -> obj.ergoTree.asJson,
      "assets" -> obj.assets.asJson,
      "additionalRegisters" -> obj.additionalRegisters.asJson
    )
  }

  implicit val decoder: Decoder[ApiOutput] = (c: HCursor) => for {
    boxId <- c.downField("boxId").as[String]
    value <- c.downField("value").as[Long]
    creationHeight <- c.downField("creationHeight").as[Int]
    ergoTree <- c.downField("ergoTree").as[String]
    assets <- c.downField("assets").as[List[ApiAsset]]
    additionalRegisters <- c.downField("additionalRegisters").as[Json]
  } yield ApiOutput(boxId, value, creationHeight, ergoTree, assets, additionalRegisters)

}
