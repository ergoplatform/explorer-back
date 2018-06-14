package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.{Input, Output, Transaction}

case class TransactionSummaryInfo(id: String,
                                  timestamp: Long,
                                  size: Int,
                                  confirmationsCount: Int,
                                  miniBlockInfo: MiniBlockInfo,
                                  inputs: List[InputInfo],
                                  outputs: List[OutputInfo],
                                  totalCoins: Long = 0L,
                                  totalFee: Long = 0L,
                                  feePerByte: Long = 0L
                                  )

object TransactionSummaryInfo {

  def fromDb(tx: Transaction, height: Int, confirmationsCount: Int = 0, inputs: List[Input], outputs: List[Output]): TransactionSummaryInfo =
    TransactionSummaryInfo(
      id = tx.id,
      miniBlockInfo = MiniBlockInfo(tx.blockId, height),
      timestamp = tx.timestamp,
      confirmationsCount = confirmationsCount,
      //TODO Need to add this data to tx
      size = 0,
      inputs = inputs.map(InputInfo.apply),
      outputs = outputs.map(OutputInfo.apply)
    )

  implicit val encoder: Encoder[TransactionSummaryInfo] = (ts: TransactionSummaryInfo) => Json.obj(
    "id" -> Json.fromString(ts.id),
    "timestamp" -> Json.fromLong(ts.timestamp),
    "size" -> Json.fromInt(ts.size),
    "confirmationsCount" -> Json.fromInt(ts.confirmationsCount),
    "block" -> ts.miniBlockInfo.asJson,
    "ioSummary" -> Json.obj(
      "totalCoinsTransferred" -> Json.fromLong(ts.totalCoins),
      "totalFee" -> Json.fromLong(ts.totalFee),
      "feePerByte" -> Json.fromLong(ts.feePerByte)
    ),
    "inputs" -> ts.inputs.asJson,
    "outputs" -> ts.outputs.asJson
  )

}
