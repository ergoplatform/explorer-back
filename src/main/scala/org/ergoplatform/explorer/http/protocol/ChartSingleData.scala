package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.StatRecord

case class ChartSingleData(timestamp: Long, value: Long)

object ChartSingleData {

  def toTotalCoinsData(s: StatRecord): ChartSingleData = ChartSingleData(s.timestamp, s.totalCoins)

  def toAvgBlockSizeData(s: StatRecord): ChartSingleData = ChartSingleData(s.timestamp, s.avgBlockSize)

  implicit val encoder: Encoder[ChartSingleData] = (d: ChartSingleData) => Json.obj(
    "timestamp" -> Json.fromLong(d.timestamp),
    "value" -> Json.fromLong(d.value)
  )
}
