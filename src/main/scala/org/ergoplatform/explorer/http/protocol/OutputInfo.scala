package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Output

case class OutputInfo(id: String, value: Long, script: String)

object OutputInfo {

  def apply(o: Output): OutputInfo = OutputInfo(o.id, o.value, o.script)

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    ("id,", Json.fromString(o.id)),
    ("value", Json.fromLong(o.value)),
    ("script", Json.fromString(o.script))
  )
}
