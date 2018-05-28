package org.ergoplatform.explorer.db.models

case class StatRecord(id: Option[Long],
                      timestamp: Long,
                      blockSize: Long,
                      totalSize: Long,
                      transactionCount: Long,
                      totalTransactionsCount: Long,
                      blocksCount: Long,
                      difficulty: Long,
                      blockCoins: Long,
                      totalCoins: Long,
                      blockValue: Long,
                      block_fee: Long,
                      totalMiningTime: Long,
                      blockMiningTime: Long
                     ) extends Entity[Option[Long]] {

  val avgBlockSize = if (blocksCount == 0L) { 0L } else { totalSize / blocksCount }
  val avgTxsCount = if (blocksCount == 0L) { 0L } else { totalTransactionsCount / blocksCount }
  val avgMiningTime = if (blocksCount == 0L) { 0L } else { totalMiningTime / blocksCount }
}

