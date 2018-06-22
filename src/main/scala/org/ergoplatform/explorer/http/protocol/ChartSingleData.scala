package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.BlockInfo

case class ChartSingleData[D: Encoder](timestamp: Long, value: D)

object ChartSingleData {

  def toTotalCoinsData(s: BlockInfo): ChartSingleData[Long] = ChartSingleData(s.timestamp, s.totalCoinsIssued)

  def toAvgBlockSizeData(s: BlockInfo): ChartSingleData[Long] = ChartSingleData(s.timestamp, s.avgBlockSize)

  implicit def encoder[D: Encoder]: Encoder[ChartSingleData[D]] = (d: ChartSingleData[D]) => Json.obj(
    "timestamp" -> Json.fromLong(d.timestamp),
    "value" -> d.value.asJson
  )
}
