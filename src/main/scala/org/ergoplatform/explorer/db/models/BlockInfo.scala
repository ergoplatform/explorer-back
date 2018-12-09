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
                      minerAddress: String,
                      minerReward: Long,
                      minerRevenue: Long,
                      blockFee: Long,
                      blockChainTotalSize: Long,
                      totalTxsCount: Long,
                      totalCoinsIssued: Long,
                      totalMiningTime: Long,
                      totalFees: Long,
                      totalMinersReward: Long,
                      totalCoinsInTxs: Long
                    ) {
  val avgBlockSize: Long = blockChainTotalSize / height
  val avgTxsCount: Long = totalTxsCount / height
  val avgMiningTime: Long = totalMiningTime / height
}

object BlockInfo {
  val empty = BlockInfo("", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, "", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
}
