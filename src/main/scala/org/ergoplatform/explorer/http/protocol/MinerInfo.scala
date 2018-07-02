package org.ergoplatform.explorer.http.protocol

import io.circe.{Decoder, Encoder, HCursor, Json}
import org.ergoplatform.explorer.db.models.Miner

case class MinerInfo(addressId: String, name: String)

object MinerInfo {

  implicit val encoder: Encoder[MinerInfo] = (minerInfo: MinerInfo) => Json.obj(
    "adressId" -> Json.fromString(minerInfo.addressId),
    "name" -> Json.fromString(minerInfo.name)
  )

  implicit val decode: Decoder[MinerInfo] = (c: HCursor) => for {
    address <- c.downField("addressId").as[String]
    name <- c.downField("name").as[String]
  } yield MinerInfo(address, name)

  def fromMiner(m: Miner): MinerInfo = MinerInfo(m.address, m.name)
}
