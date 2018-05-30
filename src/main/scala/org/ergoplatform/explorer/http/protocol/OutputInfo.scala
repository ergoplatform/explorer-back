package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Output

case class OutputInfo(id: String, value: Long, script: String, hash: String, spent: Boolean)

object OutputInfo {

  import org.ergoplatform.explorer.utils.Converter._

  def apply(o: Output): OutputInfo = OutputInfo(
    from16to58(o.id),
    o.value,
    from16to58(o.script),
    from16to58(o.hash),
    o.spent
  )

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    ("id", Json.fromString(o.id)),
    ("value", Json.fromLong(o.value)),
    ("script", Json.fromString(o.script)),
    ("hash", Json.fromString(o.hash)),
    ("spent", Json.fromBoolean(o.spent))
  )
}
