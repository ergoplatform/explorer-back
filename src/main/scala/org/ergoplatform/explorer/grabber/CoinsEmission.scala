package org.ergoplatform.explorer.grabber

import scala.annotation.tailrec

//TODO: get rid when node api will provide this data
object CoinsEmission {

  private val FixedRatePeriod = 10080
  private val FixedRate = 7500000000L
  private val EpochLength = 2160
  private val OneEpochReduction = 300000000L

  val coinsInOneErgo: Long = 100000000

  lazy val (coinsTotal, blocksTotal) = {
    @tailrec
    def loop(height: Long, acc: Long): (Long, Long) = {
      val currentRate = emissionAtHeight(height)
      if (currentRate > 0) {
        loop(height + 1L, acc + currentRate)
      } else {
        (acc, height - 1L)
      }
    }

    loop(0L, 0L)
  }

  def issuedCoinsAfterHeight(h: Long): Long = {
    if (h < FixedRatePeriod) {
      FixedRate * (h + 1)
    } else {
      val fixedRateIssue: Long = FixedRate * FixedRatePeriod
      val epoch = (h - FixedRatePeriod) / EpochLength
      val fullEpochsIssued: Long = (1 to epoch.toInt).map { e =>
        Math.max(FixedRate - OneEpochReduction * e, 0) * EpochLength
      }.sum
      val heightInThisEpoch = (h - FixedRatePeriod) % EpochLength + 1
      val rateThisEpoch = Math.max(FixedRate - OneEpochReduction * (epoch + 1), 0)
      val thisEpochIssued = heightInThisEpoch * rateThisEpoch

      fullEpochsIssued + fixedRateIssue + thisEpochIssued
    }
  }

  def remainingCoinsAfterHeight(h: Long): Long = coinsTotal - issuedCoinsAfterHeight(h)

  def emissionAtHeight(h: Long): Long = {
    if (h < FixedRatePeriod) {
      FixedRate
    } else {
      val epoch = 1 + (h - FixedRatePeriod) / EpochLength
      Math.max(FixedRate - OneEpochReduction * epoch, 0)
    }
  }.ensuring(_ >= 0, s"Negative at $h")


}
