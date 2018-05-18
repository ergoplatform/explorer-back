package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

case class MiniBlockInfo(id: String, height: Int)

object MiniBlockInfo {

  implicit val encoder: Encoder[MiniBlockInfo] = (mb: MiniBlockInfo) => Json.obj(
    ("id", Json.fromString(mb.id)),
    ("height", Json.fromInt(mb.height))
  )

}
