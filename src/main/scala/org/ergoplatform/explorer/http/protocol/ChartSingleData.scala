package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.StatRecord

case class ChartSingleData[D: Encoder](timestamp: Long, value: D)

object ChartSingleData {

  def toTotalCoinsData(s: StatRecord): ChartSingleData[Long] = ChartSingleData(s.timestamp, s.totalCoins)

  def toAvgBlockSizeData(s: StatRecord): ChartSingleData[Long] = ChartSingleData(s.timestamp, s.avgBlockSize)

  def toMarketPrice(s: StatRecord): ChartSingleData[UsdPriceInfo] = ChartSingleData(
    s.timestamp,
    UsdPriceInfo(s.marketPriceUsd)
  )

  implicit def encoder[D: Encoder]: Encoder[ChartSingleData[D]] = (d: ChartSingleData[D]) => Json.obj(
    "timestamp" -> Json.fromLong(d.timestamp),
    "value" -> d.value.asJson
  )
}
