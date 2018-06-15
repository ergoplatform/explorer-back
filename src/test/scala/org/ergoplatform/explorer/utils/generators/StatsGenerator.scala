package org.ergoplatform.explorer.utils.generators

import org.ergoplatform.explorer.db.models.StatRecord
import org.scalacheck.{Arbitrary, Gen}

object StatsGenerator {

  import Arbitrary._

  val millisInDay = 86400000

  def generateStats(howMany: Int = 100): List[StatRecord] = {
    val start = System.currentTimeMillis() - 50 * millisInDay
    val end = System.currentTimeMillis()
    val step = (end - start) / howMany
    Range.Long(start, end, step).map { ts => generateStat(ts).sample.get }.toList
  }

  def generateStat(ts: Long): Gen[StatRecord] = for {
    id <- arbLong.arbitrary
    blockSize <- arbLong.arbitrary
    totalSize <- arbLong.arbitrary
    transactionCount <- arbLong.arbitrary
    totalTransactionsCount <- arbLong.arbitrary
    blocksCount <- arbLong.arbitrary
    difficulty <- Gen.choose(0L, 10000L)
    blockCoins <- arbLong.arbitrary
    totalCoins <- arbLong.arbitrary
    blockValue <- arbLong.arbitrary
    blockFee <- arbLong.arbitrary
    totalMiningTime <- arbLong.arbitrary
    blockMiningTime <- arbLong.arbitrary
    version = "0.0.0"
    supply <- arbLong.arbitrary
    marketCap <- arbLong.arbitrary
    hashRate <- arbLong.arbitrary
  } yield StatRecord(
    id, ts, blockSize, totalSize, transactionCount, totalTransactionsCount, blocksCount,
    difficulty, blockCoins, totalCoins, blockValue, blockFee, totalMiningTime, blockMiningTime,
    version, supply, marketCap, hashRate)
}

