package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.InputWithOutputInfo

case class InputInfo(
                      id: String,
                      signature: String,
                      value: Option[Long],
                      txId: String,
                      outputTransactionId: Option[String],
                      address: Option[String])

object InputInfo {

  import org.ergoplatform.explorer.services.AddressesService._

  def fromInputWithValue(i: InputWithOutputInfo) =
    InputInfo(i.input.boxId, i.input.proofBytes, i.value, i.input.txId,
      i.outputTxId, i.address.filter(isStandardAddress))

  implicit val encoder: Encoder[InputInfo] = (i: InputInfo) => Json.obj(
    "id" -> Json.fromString(i.id),
    "address" -> i.address.asJson,
    "signature" -> Json.fromString(i.signature),
    "value" -> i.value.asJson,
    "transactionId" -> Json.fromString(i.txId),
    "outputTransactionId" -> i.outputTransactionId.asJson
  )
}
