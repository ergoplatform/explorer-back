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
                        estimatedTxVolume: Long,
                        totalMinerRevenue: Long,
                        percentEarnedTransactionsFees: Double,
                        percentTransactionVolume: Double,
                        costPerTx: Long,
                        lastDifficulty: Long,
                        totalHashrate: Long
                       )

object StatsSummary {

  val empty = StatsSummary(0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0D, 0D, 0L, 0L, 0L)

  def percentOfFee(b: BlockInfo): Double = {
    val result = b.totalMinersReward.toDouble / (b.totalMinersReward.toDouble + b.totalFees.toDouble)
    BigDecimal(result * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }

  def percentOfTxVolume(b: BlockInfo): Double = {
    val result = b.totalMinersReward.toDouble / b.totalCoinsInTxs.toDouble
    BigDecimal(result * 100).setScale(2, BigDecimal.RoundingMode.HALF_UP).toDouble
  }


  def apply(b: BlockInfo, totalUnspentOutputs: Long, totalDifficulties: Long): StatsSummary = {


    new StatsSummary(
      blocksCount = b.height,
      blocksAvgTime = b.avgMiningTime,
      totalCoins = b.totalCoinsIssued,
      totalTransactionsCount = b.totalTxsCount,
      totalFee = b.totalFees,
      totalOutput = totalUnspentOutputs,
      estimatedTxVolume = 0L,
      totalMinerRevenue = b.totalMinersReward + b.totalFees,
      percentEarnedTransactionsFees = percentOfFee(b),
      percentTransactionVolume = percentOfTxVolume(b),
      costPerTx = (b.totalMinersReward + b.totalFees) / b.totalTxsCount,
      lastDifficulty = b.difficulty,
      totalHashrate = totalDifficulties / (b.totalMiningTime / 1000)
    )
  }

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
      "estimatedTransactionVolume" -> Json.fromLong(s.estimatedTxVolume)
    ),
    "miningCost" -> Json.obj(
      "totalMinersRevenue" -> Json.fromLong(s.totalMinerRevenue),
      "percentEarnedTransactionsFees" -> Json.fromDoubleOrNull(s.percentEarnedTransactionsFees),
      "percentTransactionVolume" -> Json.fromDoubleOrNull(s.percentTransactionVolume),
      "costPerTransaction" -> Json.fromLong(s.costPerTx),
      "difficulty" -> Json.fromLong(s.lastDifficulty),
      "hashRate" -> Json.fromLong(s.totalHashrate)
    )
  )

}
