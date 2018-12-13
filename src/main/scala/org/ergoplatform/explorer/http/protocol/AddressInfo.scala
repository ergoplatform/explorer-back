package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.AddressSummaryData

case class AddressInfo(id: String, transactionsCount: Long, totalReceived: BigInt, currentBalance: Long)

object AddressInfo {

  def apply(data: AddressSummaryData): AddressInfo =
    AddressInfo(data.hash, data.txsCount, data.spent + data.unspent, data.unspent)

  implicit val encoder: Encoder[AddressInfo] = (a: AddressInfo) => Json.obj(
    "summary" -> Json.obj("id" -> Json.fromString(a.id)),
    "transactions" -> Json.obj(
      "total" -> Json.fromLong(a.transactionsCount),
      "totalReceived" -> Json.fromBigInt(a.totalReceived),
      "balance" ->  Json.fromLong(a.currentBalance)
    )
  )
}
