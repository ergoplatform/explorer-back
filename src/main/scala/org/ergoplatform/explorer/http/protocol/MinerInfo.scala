package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

final case class MinerInfo(addressId: String, name: String)

object MinerInfo {

  implicit val encoder: Encoder[MinerInfo] = { minerInfo =>
    Json.obj(
      "address" -> Json.fromString(minerInfo.addressId),
      "name"    -> Json.fromString(minerInfo.name)
    )
  }

}
