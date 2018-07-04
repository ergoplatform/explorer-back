package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.SpentOutput

case class OutputInfo(id: String, value: Long, script: String, hash: String, spentTxIs: Option[String])

object OutputInfo {

  def fromOutputWithSpent(o: SpentOutput): OutputInfo = OutputInfo(
    o.output.boxId,
    o.output.value,
    o.output.proposition,
    o.output.hash,
    o.spentTxId
  )

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    "id" -> Json.fromString(o.id),
    "value" -> Json.fromLong(o.value),
    "script" -> Json.fromString(o.script),
    "address" -> Json.fromString(o.hash),
    "spentTransactionId" -> o.spentTxIs.asJson
  )
}
