package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

final case class SearchInfo(
  blocks: List[SearchBlockInfo],
  transactionIds: List[String],
  addresses: List[String]
)

object SearchInfo {

  implicit val encoder: Encoder[SearchInfo] = { i =>
    Json.obj(
      "blocks"       -> i.blocks.asJson,
      "transactions" -> i.transactionIds.asJson,
      "addresses"    -> i.addresses.asJson
    )
  }

}
