package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

final case class MiniBlockInfo(id: String, height: Long)

object MiniBlockInfo {

  implicit val encoder: Encoder[MiniBlockInfo] = { mb =>
    Json.obj(
      ("id", Json.fromString(mb.id)),
      ("height", Json.fromLong(mb.height))
    )
  }

}
