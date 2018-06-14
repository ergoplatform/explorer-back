package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Output

case class OutputInfo(id: String, value: Long, script: String, hash: String)

object OutputInfo {


  def apply(o: Output): OutputInfo = OutputInfo(
    o.id,
    o.value,
    o.script,
    if (o.hash.startsWith("cd0703")) {o.hash} else { "Unable to decode output address."}
  )

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    "id" -> Json.fromString(o.id),
    "value" -> Json.fromLong(o.value),
    "script" -> Json.fromString(o.script),
    "address" -> Json.fromString(o.hash),
  )
}
