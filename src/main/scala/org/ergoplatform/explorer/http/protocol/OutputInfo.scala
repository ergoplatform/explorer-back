package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{ExtendedOutput, Output}

final case class OutputInfo(
  id: String,
  value: Long,
  creationHeight: Int,
  ergoTree: String,
  address: String,
  assets: Json,
  additionalRegisters: Json,
  spentTxIs: Option[String],
  mainChain: Boolean
)

object OutputInfo {

  def fromOutputWithSpent(o: ExtendedOutput): OutputInfo = OutputInfo(
    o.output.boxId,
    o.output.value,
    o.output.creationHeight,
    o.output.ergoTree,
    o.output.address,
    o.output.assets,
    o.output.additionalRegisters,
    o.spentTxId,
    o.mainChain
  )

  def fromOutput(o: Output): OutputInfo = OutputInfo(
    o.boxId,
    o.value,
    o.creationHeight,
    o.ergoTree,
    o.address,
    o.assets,
    o.additionalRegisters,
    None,
    mainChain = true
  )

  implicit val encoder: Encoder[OutputInfo] = { oi =>
    Json.obj(
      "id"                  -> oi.id.asJson,
      "value"               -> oi.value.asJson,
      "creationHeight"      -> oi.creationHeight.asJson,
      "ergoTree"            -> oi.ergoTree.asJson,
      "address"             -> oi.address.asJson,
      "assets"              -> oi.assets,
      "additionalRegisters" -> oi.additionalRegisters,
      "spentTransactionId"  -> oi.spentTxIs.asJson,
      "mainChain"           -> oi.mainChain.asJson
    )
  }

}
