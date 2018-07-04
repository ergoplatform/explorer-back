package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.SpentOutput

case class AddressInfo(id: String, transactionsCount: Int, totalReceived: Long, currentBalance: Long)

object AddressInfo {

  def apply(addressId: String, spentOutput: List[SpentOutput]): AddressInfo = {
    val related = spentOutput.filter(_.output.hash == addressId)
    val count = related.map(_.output.txId).distinct.length
    val receivedSummary = related.map(_.output.value).sum
    val currentBalance = related.filter(_.spentTxId.isEmpty).map(_.output.value).sum

    AddressInfo(addressId, count, receivedSummary, currentBalance)
  }

  implicit val encoder: Encoder[AddressInfo] = (a: AddressInfo) => Json.obj(
    "summary" -> Json.obj("id" -> Json.fromString(a.id)),
    "transactions" -> Json.obj(
      "total" -> Json.fromInt(a.transactionsCount),
      "totalReceived" -> Json.fromLong(a.totalReceived),
      "balance" ->  Json.fromLong(a.currentBalance)
    )
  )
}
