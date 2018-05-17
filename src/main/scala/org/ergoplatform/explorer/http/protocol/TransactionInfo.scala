package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{Input, Output, Transaction}

case class TransactionInfo(id: String, inputs: List[InputInfo], outputs: List[OutputInfo])

object TransactionInfo {

  /**
    * This method forms transactions info from soup of raw txs, inputs, outputs
    *
    * @param txs     list of all transactions
    * @param inputs  list of all inputs
    * @param outputs list of all outputs
    */
  def extractInfo(txs: List[Transaction], inputs: List[Input], outputs: List[Output]): List[TransactionInfo] =
    txs.map { tx =>
      val relatedInputs = inputs.filter(_.txId == tx.id).map(InputInfo.apply)
      val relatedOutputs = outputs.filter(_.txId == tx.id).map(OutputInfo.apply)
      apply(tx.id, relatedInputs, relatedOutputs)
    }

  implicit val encoder: Encoder[TransactionInfo] = (tx: TransactionInfo) => Json.obj(
    ("id", Json.fromString(tx.id)),
    ("inputs", tx.inputs.asJson),
    ("outputs", tx.outputs.asJson)
  )

}
