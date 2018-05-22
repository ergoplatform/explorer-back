package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Output
import org.ergoplatform.explorer.utils.Converter._

case class AddressInfo(id: String, transactionsCount: Int, totalReceived: Long, currentBalance: Long)

object AddressInfo {

  def apply(addressId: String, os: List[Output]): AddressInfo = {
    val related = os.filter(_.hash == addressId)
    val count = related.map(_.txId).distinct.length
    val receivedSummary = related.map(_.value).sum
    val currentBalance = related.filter(_.spent == false).map(_.value).sum

    AddressInfo(from16to58(addressId), count, receivedSummary, currentBalance)
  }

  implicit val encoder: Encoder[AddressInfo] = (a: AddressInfo) => Json.obj(
    ("id", Json.fromString(a.id)),
    ("transactions", Json.obj(
      ("total", Json.fromInt(a.transactionsCount)),
      ("totalReceived", Json.fromLong(a.totalReceived)),
      ("balance", Json.fromLong(a.currentBalance)))
    )
  )
}
