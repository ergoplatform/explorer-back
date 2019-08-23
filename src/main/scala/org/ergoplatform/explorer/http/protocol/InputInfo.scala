package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.InputWithOutputInfo

final case class InputInfo(
  id: String,
  proof: String,
  value: Option[Long],
  txId: String,
  outputTransactionId: Option[String],
  address: Option[String]
)

object InputInfo {

  def fromInputWithValue(i: InputWithOutputInfo) = InputInfo(
    i.input.boxId,
    i.input.proofBytes,
    i.value,
    i.input.txId,
    i.outputTxId,
    i.address
  )

  implicit val encoder: Encoder[InputInfo] = { i =>
    Json.obj(
      "id"                  -> Json.fromString(i.id),
      "address"             -> i.address.asJson,
      "spendingProof"       -> Json.fromString(i.proof),
      "value"               -> i.value.asJson,
      "transactionId"       -> Json.fromString(i.txId),
      "outputTransactionId" -> i.outputTransactionId.asJson
    )
  }

}
