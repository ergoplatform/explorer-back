package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

case class MinerInfo(addressId: String, name: String)

object MinerInfo {

  implicit val encoder: Encoder[MinerInfo] = (minerInfo: MinerInfo) => Json.obj(
    "adressId" -> Json.fromString(minerInfo.addressId),
    "name" -> Json.fromString(minerInfo.name)
  )
}
