package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Output

case class OutputInfo(id: String, value: Long, script: String, hash: Option[String])

object OutputInfo {


  def apply(o: Output): OutputInfo = OutputInfo(
    o.id,
    o.value,
    o.proposition,
    Some(o.hash).filter(_.startsWith("cd0703"))
  )

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    "id" -> Json.fromString(o.id),
    "value" -> Json.fromLong(o.value),
    "script" -> Json.fromString(o.script),
    "address" -> o.hash.fold(Json.Null) { v => Json.fromString(v) },
  )
}
