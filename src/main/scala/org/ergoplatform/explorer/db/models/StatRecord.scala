package org.ergoplatform.explorer.db.models

case class StatRecord(id: Long = -1L,
                      timestamp: Long = 0L,
                      blockSize: Long = 0L,
                      totalSize: Long = 0L,
                      transactionCount: Long = 0L,
                      totalTransactionsCount: Long = 0L,
                      blocksCount: Long = 0L,
                      difficulty: Long = 0L,
                      blockCoins: Long = 0L,
                      totalCoins: Long = 0L,
                      blockValue: Long = 0L,
                      block_fee: Long = 0L,
                      totalMiningTime: Long = 0L,
                      blockMiningTime: Long = 0L,
                      version: String = "0.0.0",
                      supply: Long = 0L,
                      marketCap: Long = 0L,
                      hashRate: Long = 0L,
                      marketPriceUsd: Long = 0L,
                      marketPriceBtc: Long = 0L
                     ) extends Entity[Long] {

  val avgBlockSize = if (blocksCount == 0L) { 0L } else { totalSize / blocksCount }
  val avgTxsCount = if (blocksCount == 0L) { 0L } else { totalTransactionsCount / blocksCount }
  val avgMiningTime = if (blocksCount == 0L) { 0L } else { totalMiningTime / blocksCount }
}

