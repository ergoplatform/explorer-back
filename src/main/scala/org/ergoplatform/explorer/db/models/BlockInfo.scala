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
  val avgBlockSize: Long = if (height != 0) blockChainTotalSize / height else 0L
  val avgTxsCount: Long = if (height != 0) totalTxsCount / height else 0L
  val avgMiningTime: Long = if (height != 0) totalMiningTime / height else 0L
}

object BlockInfo {
  val empty = BlockInfo("", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, "", 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L, 0L)
}
