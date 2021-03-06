package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Asset
import org.ergoplatform.explorer.db.models.composite.ExtendedOutput

final case class OutputInfo(
  id: String,
  value: Long,
  creationHeight: Int,
  ergoTree: String,
  address: String,
  assets: Seq[AssetInfo],
  additionalRegisters: Json,
  spentTxIs: Option[String],
  mainChain: Boolean
)

object OutputInfo {

  def apply(o: ExtendedOutput, assets: List[Asset]): OutputInfo =
    OutputInfo(
      o.output.boxId,
      o.output.value,
      o.output.creationHeight,
      o.output.ergoTree,
      o.output.address,
      assets.map(x => AssetInfo(x.id, x.amount)),
      o.output.additionalRegisters,
      o.spentByOpt,
      o.mainChain
    )

  implicit val encoder: Encoder[OutputInfo] = { oi =>
    Json.obj(
      "id"                  -> oi.id.asJson,
      "value"               -> oi.value.asJson,
      "creationHeight"      -> oi.creationHeight.asJson,
      "ergoTree"            -> oi.ergoTree.asJson,
      "address"             -> oi.address.asJson,
      "assets"              -> oi.assets.asJson,
      "additionalRegisters" -> oi.additionalRegisters,
      "spentTransactionId"  -> oi.spentTxIs.asJson,
      "mainChain"           -> oi.mainChain.asJson
    )
  }

}
