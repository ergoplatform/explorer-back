package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models._
import org.ergoplatform.explorer.db.models.composite.{ExtendedInput, ExtendedOutput}

final case class TransactionSummaryInfo(
  id: String,
  timestamp: Long,
  size: Long,
  confirmationsCount: Long,
  miniBlockInfo: MiniBlockInfo,
  inputs: List[InputInfo],
  outputs: List[OutputInfo],
  totalCoins: Long = 0L,
  totalFee: Long = 0L,
  feePerByte: Long = 0L
)

object TransactionSummaryInfo {

  def apply(
    tx: Transaction,
    height: Long,
    confirmationsCount: Long,
    inputs: List[ExtendedInput],
    outputs: List[(ExtendedOutput, List[Asset])]
  ): TransactionSummaryInfo = {
    val totalFee = outputs.filter(_._1.output.ergoTree == "0101").map(_._1.output.value).sum // todo: move "0101" to constants
    val feePerByte = if (tx.size == 0) 0L else totalFee / tx.size

    TransactionSummaryInfo(
      id = tx.id,
      miniBlockInfo = MiniBlockInfo(tx.headerId, height),
      timestamp = tx.timestamp,
      confirmationsCount = confirmationsCount + 1L,
      size = tx.size,
      inputs = inputs.map(InputInfo.fromExtendedInput),
      outputs = outputs.map(x => OutputInfo(x._1, x._2)),
      totalCoins = inputs.map(_.value.getOrElse(0L)).sum,
      totalFee = totalFee,
      feePerByte = feePerByte
    )
  }

  implicit val encoder: Encoder[TransactionSummaryInfo] = { ts =>
    Json.obj(
      "summary" -> Json.obj(
        "id"                 -> Json.fromString(ts.id),
        "timestamp"          -> Json.fromLong(ts.timestamp),
        "size"               -> Json.fromLong(ts.size),
        "confirmationsCount" -> Json.fromLong(ts.confirmationsCount),
        "block"              -> ts.miniBlockInfo.asJson
      ),
      "ioSummary" -> Json.obj(
        "totalCoinsTransferred" -> Json.fromLong(ts.totalCoins),
        "totalFee"              -> Json.fromLong(ts.totalFee),
        "feePerByte"            -> Json.fromLong(ts.feePerByte)
      ),
      "inputs"  -> ts.inputs.asJson,
      "outputs" -> ts.outputs.asJson
    )
  }

}
