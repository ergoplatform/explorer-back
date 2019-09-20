package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models._
import org.ergoplatform.explorer.db.models.composite.{ExtendedInput, ExtendedOutput}

final case class TransactionInfo(
  id: String,
  timestamp: Long,
  confirmationsQty: Long,
  inputs: List[InputInfo],
  outputs: List[OutputInfo]
)

object TransactionInfo {

  /**
    * This method forms transactions info from soup of raw txs, inputs, outputs
    *
    * @param txs     list of all transactions
    * @param inputs  list of all inputs
    * @param outputs list of all outputs
    */
  def fromBatch(
    txs: List[Transaction],
    confirmations: List[(String, Long)],
    inputs: List[ExtendedInput],
    outputs: List[(ExtendedOutput, List[Asset])]
  ): List[TransactionInfo] =
    txs.map { tx =>
      val relatedInputs = inputs
        .filter(_.input.txId == tx.id)
        .map(InputInfo.fromExtendedInput)
      val relatedOutputs = outputs
        .filter(_._1.output.txId == tx.id)
        .map {
          case (out, assets) =>
            OutputInfo(out, assets)
        }
      val id = tx.id
      val ts = tx.timestamp
      val confirmationCount = confirmations
        .find(_._1 == id)
        .map(_._2)
        .getOrElse(0L)
      apply(id, ts, confirmationCount, relatedInputs, relatedOutputs)
    }

  def apply(
    tx: Transaction,
    confirmationsQty: Long,
    inputs: List[ExtendedInput],
    outputs: List[(ExtendedOutput, List[Asset])]
  ): TransactionInfo = {
    val inputsInfo = inputs.map(InputInfo.fromExtendedInput)
    val outputsInfo = outputs.map {
      case (out, assets) =>
        OutputInfo(out, assets)
    }
    new TransactionInfo(tx.id, tx.timestamp, confirmationsQty, inputsInfo, outputsInfo)
  }

  implicit val encoder: Encoder[TransactionInfo] = { tx =>
    Json.obj(
      "id"                 -> Json.fromString(tx.id),
      "timestamp"          -> Json.fromLong(tx.timestamp),
      "confirmationsCount" -> Json.fromLong(tx.confirmationsQty),
      "inputs"             -> tx.inputs.asJson,
      "outputs"            -> tx.outputs.asJson
    )
  }

}
