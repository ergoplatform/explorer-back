package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

final case class MinerStatSingleInfo(name: String, value: Long)

object MinerStatSingleInfo {

  implicit val e: Encoder[MinerStatSingleInfo] = { m =>
    Json.obj(
      "name"  -> Json.fromString(m.name),
      "value" -> Json.fromLong(m.value)
    )
  }

}
