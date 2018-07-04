package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

case class MinerStatSingleInfo(name: String, value:  Long)

object MinerStatSingleInfo {

  implicit val e: Encoder[MinerStatSingleInfo] = (m: MinerStatSingleInfo) => {
    Json.obj(
      "name" -> Json.fromString(m.name),
      "value" -> Json.fromLong(m.value)
    )
  }
}
