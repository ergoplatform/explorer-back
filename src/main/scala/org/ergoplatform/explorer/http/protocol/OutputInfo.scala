package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{Output, ExtendedOutput}

final case class OutputInfo(id: String,
                            value: Long,
                            creationHeight: Int,
                            ergoTree: String,
                            address: String,
                            assets: Json,
                            additionalRegisters: Json,
                            spentTxIs: Option[String],
                            mainChain: Boolean)

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

  implicit val encoder: Encoder[OutputInfo] = (o: OutputInfo) => Json.obj(
    "id" -> o.id.asJson,
    "value" -> o.value.asJson,
    "creationHeight" -> o.creationHeight.asJson,
    "ergoTree" -> o.ergoTree.asJson,
    "address" -> o.address.asJson,
    "assets" -> o.assets,
    "additionalRegisters" -> o.additionalRegisters,
    "spentTransactionId" -> o.spentTxIs.asJson,
    "mainChain" -> o.mainChain.asJson
  )
}
