package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.StatRecord

case class StatsSummary(blocksCount: Long, blocksAvgTime: Long, totalCoins: Long, totalTransactionsCount: Long)

object StatsSummary {

  def apply(s: StatRecord): StatsSummary = new StatsSummary(
    blocksCount = s.blocksCount,
    blocksAvgTime = s.avgMiningTime,
    totalCoins = s.totalCoins,
    totalTransactionsCount = s.totalTransactionsCount
  )

  implicit val encoder: Encoder[StatsSummary] = (s: StatsSummary) => Json.obj(
    "blockSummary" -> Json.obj(
      "total" -> Json.fromLong(s.blocksCount),
      "averageMiningTime" -> Json.fromLong(s.blocksAvgTime),
      "totalCoins" -> Json.fromLong(s.totalCoins)
    ),
    "transactionsSummary" -> Json.obj(
      "total" -> Json.fromLong(s.totalTransactionsCount)
    )
  )

}
