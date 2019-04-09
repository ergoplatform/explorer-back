package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{Output, SpentOutput}

case class OutputInfo(id: String,
                      value: Long,
                      creationHeight: Int,
                      ergoTree: String,
                      address: String,
                      assets: Json,
                      additionalRegisters: Json,
                      spentTxIs: Option[String])

object OutputInfo {

  def fromOutputWithSpent(o: SpentOutput): OutputInfo = OutputInfo(
    o.output.boxId,
    o.output.value,
    o.output.creationHeight,
    o.output.ergoTree,
    o.output.address,
    o.output.assets,
    o.output.additionalRegisters,
    o.spentTxId
  )

  def fromOutput(o: Output): OutputInfo = OutputInfo(
    o.boxId,
    o.value,
    o.creationHeight,
    o.ergoTree,
    o.address,
    o.assets,
    o.additionalRegisters,
    None
  )

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    "id" -> Json.fromString(o.id),
    "value" -> Json.fromLong(o.value),
    "ergoTree" -> Json.fromString(o.ergoTree),
    "address" -> Json.fromString(o.address),
    "spentTransactionId" -> o.spentTxIs.asJson
  )
}
