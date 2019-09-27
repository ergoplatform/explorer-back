package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

final case class AssetInfo(assetId: String, amount: Long)

object AssetInfo {
  implicit val encoder: Encoder[AssetInfo] = { ai =>
    Json.obj(
      "tokenId" -> ai.assetId.asJson,
      "amount"  -> ai.amount.asJson
    )
  }
}
