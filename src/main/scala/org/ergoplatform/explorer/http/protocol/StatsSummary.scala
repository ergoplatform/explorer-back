package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.BlockInfo

import scala.math.BigDecimal

case class StatsSummary(blocksCount: Long,
                        blocksAvgTime: Long,
                        totalCoins: Long,
                        totalTransactionsCount: Long,
                        totalFee: Long,
                        totalOutput: Long,
                        estimatedOutput: BigInt,
                        totalMinerRevenue: Long,
                        percentEarnedTransactionsFees: Double,
                        percentTransactionVolume: Double,
                        costPerTx: Long,
                        lastDifficulty: Long,
                        totalHashrate: Long)

object StatsSummary {

  val empty = StatsSummary(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0D, 0D, 0L, 0L, 0L)

  implicit val encoder: Encoder[StatsSummary] = (s: StatsSummary) => Json.obj(
    "blockSummary" -> Json.obj(
      "total" -> Json.fromLong(s.blocksCount),
      "averageMiningTime" -> Json.fromLong(s.blocksAvgTime),
      "totalCoins" -> Json.fromLong(s.totalCoins)
    ),
    "transactionsSummary" -> Json.obj(
      "total" -> Json.fromLong(s.totalTransactionsCount),
      "totalFee" -> Json.fromLong(s.totalFee),
      "totalOutput" -> Json.fromLong(s.totalOutput),
      "estimatedTransactionVolume" -> Json.fromBigInt(s.estimatedOutput)
    ),
    "miningCost" -> Json.obj(
      "totalMinersRevenue" -> Json.fromLong(s.totalMinerRevenue),
      "percentEarnedTransactionsFees" -> Json.fromDoubleOrNull(s.percentEarnedTransactionsFees.floatValue()),
      "percentTransactionVolume" -> Json.fromDoubleOrNull(s.percentTransactionVolume.floatValue()),
      "costPerTransaction" -> Json.fromLong(s.costPerTx),
      "difficulty" -> Json.fromLong(s.lastDifficulty),
      "hashRate" -> Json.fromLong(s.totalHashrate)
    )
  )

}
