package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.grabber.protocol.ApiAsset

case class AddressInfo(id: String,
                       confirmedTxsQty: Long,
                       totalReceived: BigInt,
                       confirmedBalance: Long,
                       totalBalance: Long,
                       confirmedTokensBalance: List[ApiAsset],
                       totalTokensBalance: List[ApiAsset])

object AddressInfo {

  implicit val encoder: Encoder[AddressInfo] = { a: AddressInfo =>
    Json.obj(
      "summary" -> Json.obj("id" -> a.id.asJson),
      "transactions" -> Json.obj(
        "confirmed" -> a.confirmedTxsQty.asJson,
        "totalReceived" -> a.totalReceived.asJson,
        "confirmedBalance" ->  a.confirmedBalance.asJson,
        "totalBalance" ->  a.totalBalance.asJson,
        "confirmedTokensBalance" -> a.confirmedTokensBalance.asJson,
        "totalTokensBalance" -> a.totalTokensBalance.asJson
      )
    )
  }

}
