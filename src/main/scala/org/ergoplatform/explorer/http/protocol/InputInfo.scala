package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.InputWithOutputInfo

case class InputInfo(id: String, signature: String, value: Long, txId: String, outputTransactionId: String)

object InputInfo {

  def fromInputWithValue(i: InputWithOutputInfo) =
    InputInfo(i.input.boxId, i.input.proofBytes, i.value, i.input.txId, i.outputTxId)

  implicit val encoder: Encoder[InputInfo] = (i: InputInfo) => Json.obj(
    "id" -> Json.fromString(i.id),
    "signature" -> Json.fromString(i.signature),
    "value" -> i.value.asJson,
    "transactionId" -> Json.fromString(i.txId),
    "outputTransactionId" -> Json.fromString(i.outputTransactionId)
  )
}
