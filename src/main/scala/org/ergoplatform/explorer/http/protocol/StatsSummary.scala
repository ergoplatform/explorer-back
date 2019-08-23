package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.BlockInfo

import scala.math.BigDecimal

final case class StatsSummary(
  blocksCount: Long,
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
  totalHashrate: Long
)

object StatsSummary {

  val empty = StatsSummary(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0D, 0D, 0L, 0L, 0L)

  implicit val encoder: Encoder[StatsSummary] = { ss =>
    Json.obj(
      "blockSummary" -> Json.obj(
        "total"             -> Json.fromLong(ss.blocksCount),
        "averageMiningTime" -> Json.fromLong(ss.blocksAvgTime),
        "totalCoins"        -> Json.fromLong(ss.totalCoins)
      ),
      "transactionsSummary" -> Json.obj(
        "total"                      -> Json.fromLong(ss.totalTransactionsCount),
        "totalFee"                   -> Json.fromLong(ss.totalFee),
        "totalOutput"                -> Json.fromLong(ss.totalOutput),
        "estimatedTransactionVolume" -> Json.fromBigInt(ss.estimatedOutput)
      ),
      "miningCost" -> Json.obj(
        "totalMinersRevenue" -> Json.fromLong(ss.totalMinerRevenue),
        "percentEarnedTransactionsFees" -> Json.fromDoubleOrNull(
          ss.percentEarnedTransactionsFees.floatValue()
        ),
        "percentTransactionVolume" -> Json.fromDoubleOrNull(
          ss.percentTransactionVolume.floatValue()
        ),
        "costPerTransaction" -> Json.fromLong(ss.costPerTx),
        "difficulty"         -> Json.fromLong(ss.lastDifficulty),
        "hashRate"           -> Json.fromLong(ss.totalHashrate)
      )
    )
  }

}
