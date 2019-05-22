package org.ergoplatform.explorer.db.models

import io.circe.Json
import org.ergoplatform.explorer.grabber.protocol.ApiAsset

case class Output(boxId: String,
                  txId: String,
                  value: Long,
                  creationHeight: Int,
                  index: Int,
                  ergoTree: String,
                  address: String,
                  assets: Json = Json.Null,
                  additionalRegisters: Json = Json.Null,
                  timestamp: Long) {

  def encodedAssets: Map[String, Long] =
    assets.as[List[ApiAsset]].fold(
      _ => throw new IllegalStateException("Failed to decode data from db"),
      _.map(x => x.tokenId -> x.amount).toMap
    )

}
