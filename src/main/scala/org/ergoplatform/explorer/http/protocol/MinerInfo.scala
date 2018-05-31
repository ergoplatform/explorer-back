package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.utils.Converter._
import scorex.crypto.encode.Base16

case class MinerInfo(addressId: String, name: String)

object MinerInfo {

  def apply(addressBytes: Array[Byte], name: String = "UNKNOWN"): MinerInfo = MinerInfo(
    Base16.encode(addressBytes),
    name
  )

  implicit val ecoderMinerInfo: Encoder[MinerInfo] = (minerInfo: MinerInfo) => Json.obj(
    ("adressId", Json.fromString(from16to58(minerInfo.addressId))),
    ("name", Json.fromString(minerInfo.name))
  )
}
