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
                    )
