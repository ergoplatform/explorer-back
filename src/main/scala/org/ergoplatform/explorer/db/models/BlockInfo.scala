package org.ergoplatform.explorer.db.models

case class BlockInfo(
                      headerId: String,
                      timestamp: Long,
                      height: Long,
                      difficulty: Long,
                      blockSize: Long,
                      blockCoins: Long,
                      blockMiningTime: Long,
                      txsCount: Long,
                      txsSize: Long,
                      minerName: String,
                      minerAddress: String,
                      minerReward: Long,
                      minerRevenue: Long,
                      blockFee: Long,
                      blockChainTotalSize: Long,
                      totalTxsCount: Long,
                      totalCoinsIssued: Long,
                      totalMiningTime: Long,
                      totalMinerRevenue: Long
                    ) {
  val avgBlockSize = if (height == 0L) { 0L } else { blockChainTotalSize / height }
  val avgTxsCount = if (height == 0L) { 0L } else { totalTxsCount / height }
  val avgMiningTime = if (height == 0L) { 0L } else { totalMiningTime / height }
}
