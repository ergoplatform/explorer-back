package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.BlockInfo

case class StatsSummary(blocksCount: Long,
                        blocksAvgTime: Long,
                        totalCoins: Long,
                        totalTransactionsCount: Long,
                        totalFee: Long,
                        totalOutput: Long)

object StatsSummary {

  val empty = StatsSummary(0L, 0L, 0L, 0L, 0L, 0L)

  def apply(s: BlockInfo): StatsSummary = new StatsSummary(
    blocksCount = s.height,
    blocksAvgTime = s.avgMiningTime,
    totalCoins = s.totalCoinsIssued,
    totalTransactionsCount = s.totalTxsCount,
    0L,
    0L
  )

  implicit val encoder: Encoder[StatsSummary] = (s: StatsSummary) => Json.obj(
    "blockSummary" -> Json.obj(
      "total" -> Json.fromLong(s.blocksCount),
      "averageMiningTime" -> Json.fromLong(s.blocksAvgTime),
      "totalCoins" -> Json.fromLong(s.totalCoins)
    ),
    "transactionsSummary" -> Json.obj(
      "total" -> Json.fromLong(s.totalTransactionsCount),
      "totalFee" -> Json.fromLong(s.totalFee),
      "totalOutput" -> Json.fromLong(s.totalOutput)
    ),
    "miningCost" -> Json.obj(
      "totalMinersRevenue" -> Json.fromLong(0L),
      "percentEarnedTransactionsFees" -> Json.fromLong(0L),
      "percentTransactionVolume" -> Json.fromLong(0L),
      "costPerTransaction" -> Json.fromLong(4323000L),
      "difficulty" -> Json.fromLong(2340990000L),
      "hashRate" -> Json.fromLong(90349095)
    )
  )

}
