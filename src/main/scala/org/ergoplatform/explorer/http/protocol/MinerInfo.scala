package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import scorex.crypto.encode.Base16

case class MinerInfo(addressId: String, name: String)

object MinerInfo {

  def apply(addressBytes: Array[Byte], name: String = "UNKNOWN"): MinerInfo = MinerInfo(
    Base16.encode(addressBytes),
    name
  )

  implicit val ecoderMinerInfo: Encoder[MinerInfo] = (minerInfo: MinerInfo) => Json.obj(
    ("adressId", Json.fromString(minerInfo.addressId)),
    ("name", Json.fromString(minerInfo.name))
  )
}
