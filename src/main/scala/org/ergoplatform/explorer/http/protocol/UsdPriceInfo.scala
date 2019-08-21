package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json, JsonNumber}

final case class UsdPriceInfo(value: Long)

object UsdPriceInfo {

  implicit val encoder: Encoder[UsdPriceInfo] = { usd =>
    val bucks = usd.value / 100
    val pennies = usd.value % 100
    val str = s"$bucks.${"%02d".format(pennies)}"
    Json.fromJsonNumber(JsonNumber.fromDecimalStringUnsafe(str))
  }

}
