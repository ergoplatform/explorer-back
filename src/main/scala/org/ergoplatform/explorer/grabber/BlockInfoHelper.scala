package org.ergoplatform.explorer.grabber

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.scalalogging.Logger
import org.ergoplatform.explorer.db.models.BlockInfo
import org.ergoplatform.explorer.grabber.db.BlockInfoWriter
import org.ergoplatform.explorer.grabber.protocol.ApiFullBlock

import scala.concurrent.duration._

object BlockInfoHelper {

  val logger = Logger("blocks-info-helper")

  val blockInfoCache: Cache[String, BlockInfo] = Scaffeine()
    .recordStats()
    .expireAfterWrite(2 minutes)
    .maximumSize(1000)
    .build[String, BlockInfo]()

  private val BYTES = "968400020191a3c6a70300059784000201968400030193c2a7c2b2a505000000000000000093958fa30500000000000027600500000001bf08eb00990500000001bf08eb009c050000000011e1a3009a0500000000000000019d99a305000000000000276005000000000000087099c1a7c1b2a505000000000000000093c6b2a5050000000000000000030005a390c1a7050000000011e1a300"

  private def minerAddress(nfb: ApiFullBlock): String = {
    val tx = nfb.bt.transactions.find(t => t.outputs.headOption.exists(_.proposition ==  BYTES))
    tx.fold("unknown_miner") { t =>
      t.outputs.drop(1).headOption.map{v => v.proposition}.getOrElse("unknown_miner")
    }

  }

  private def minerRewardAndFee(nfb: ApiFullBlock): (Long, Long) = {
    val reward = CoinsEmission.emissionAtHeight(nfb.header.height)
    val tx = nfb.bt.transactions.find(t => t.outputs.headOption.contains(BYTES))
    val fee = tx.fold(0L) { t =>
      t.outputs.drop(1).headOption.map { v => v.value - reward }.getOrElse(0L)
    }
    (reward, fee)
  }

  def extractBlockInfo(nfb: ApiFullBlock): BlockInfoWriter.ToInsert = {
    val (reward, fee) = minerRewardAndFee(nfb)
    val mAddress = minerAddress(nfb)
    val blockInfo = if (nfb.header.height == 0) {
      BlockInfo(
        nfb.header.id,
        nfb.header.timestamp,
        nfb.header.height,
        nfb.header.difficulty.value.toLong,
        0L,
        nfb.bt.transactions.flatMap(_.outputs).map(_.value).sum,
        0L,
        nfb.bt.transactions.length.toLong,
        0L,
        mAddress,
        mAddress,
        reward,
        reward + fee,
        fee,
        0L,
        nfb.bt.transactions.length.toLong,
        CoinsEmission.issuedCoinsAfterHeight(nfb.header.height),
        0L,
        fee,
        reward,
        nfb.bt.transactions.flatMap(_.outputs).map(_.value).sum
      )
    } else {
      logger.trace("Getting prev blockInfo from cache.")
      val prev = blockInfoCache.getIfPresent(nfb.header.parentId).get
      val miningTime = nfb.header.timestamp - prev.timestamp

      BlockInfo(
        nfb.header.id,
        nfb.header.timestamp,
        nfb.header.height,
        nfb.header.difficulty.value.toLong,
        0L,
        nfb.bt.transactions.flatMap(_.outputs).map(_.value).sum,
        nfb.header.timestamp - prev.timestamp,
        nfb.bt.transactions.length.toLong,
        0L,
        mAddress,
        mAddress,
        reward,
        reward + fee,
        fee,
        0L,
        nfb.bt.transactions.length.toLong + prev.totalTxsCount,
        CoinsEmission.issuedCoinsAfterHeight(nfb.header.height),
        prev.totalMiningTime + miningTime,
        prev.totalFees + fee,
        prev.totalMinersReward + reward,
        prev.totalCoinsInTxs + nfb.bt.transactions.flatMap(_.outputs).map(_.value).sum
      )
    }
    logger.trace(s"Putting block info for height ${blockInfo.height} into cache.")
    blockInfoCache.put(blockInfo.headerId, blockInfo)
    blockInfo
  }

}
