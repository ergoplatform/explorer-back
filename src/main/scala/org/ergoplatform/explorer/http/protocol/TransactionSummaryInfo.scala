package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models._

case class TransactionSummaryInfo(id: String,
                                  timestamp: Long,
                                  size: Int,
                                  confirmationsCount: Long,
                                  miniBlockInfo: MiniBlockInfo,
                                  inputs: List[InputInfo],
                                  outputs: List[OutputInfo],
                                  totalCoins: Long = 0L,
                                  totalFee: Long = 0L,
                                  feePerByte: Long = 0L
                                  )

object TransactionSummaryInfo {

  def fromDb(tx: Transaction,
             height: Long,
             confirmationsCount: Long = 0,
             inputs: List[InputWithOutputInfo],
             outputs: List[SpentOutput]): TransactionSummaryInfo =
    TransactionSummaryInfo(
      id = tx.id,
      miniBlockInfo = MiniBlockInfo(tx.headerId, height),
      timestamp = tx.timestamp,
      confirmationsCount = confirmationsCount,
      //TODO Need to add this data to tx
      size = 0,
      inputs = inputs.map(InputInfo.fromInputWithValue),
      outputs = outputs.map(OutputInfo.fromOutputWithSpent),
      totalCoins = inputs.map(_.value).sum
    )

  implicit val encoder: Encoder[TransactionSummaryInfo] = (ts: TransactionSummaryInfo) => Json.obj(
    "summary" -> Json.obj(
      "id" -> Json.fromString(ts.id),
      "timestamp" -> Json.fromLong(ts.timestamp),
      "size" -> Json.fromInt(ts.size),
      "confirmationsCount" -> Json.fromLong(ts.confirmationsCount),
      "block" -> ts.miniBlockInfo.asJson
    ),
    "ioSummary" -> Json.obj(
      "totalCoinsTransferred" -> Json.fromLong(ts.totalCoins),
      "totalFee" -> Json.fromLong(ts.totalFee),
      "feePerByte" -> Json.fromLong(ts.feePerByte)
    ),
    "inputs" -> ts.inputs.asJson,
    "outputs" -> ts.outputs.asJson
  )

}
