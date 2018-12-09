package org.ergoplatform.explorer.grabber

import cats.data.NonEmptyList
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.scalalogging.Logger
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.explorer.config.NetworkConfig
import org.ergoplatform.explorer.db.models.BlockInfo
import org.ergoplatform.explorer.grabber.db.BlockInfoWriter
import org.ergoplatform.explorer.grabber.protocol.{ApiFullBlock, ApiTransaction}
import scorex.util.encode.Base16
import sigmastate.serialization.ValueSerializer

import scala.concurrent.duration._

class BlockInfoHelper(networkConfig: NetworkConfig) {

  val logger = Logger("blocks-info-helper")

  val blockInfoCache: Cache[String, BlockInfo] = Scaffeine()
    .recordStats()
    .expireAfterWrite(10 minutes)
    .maximumSize(1000)
    .build[String, BlockInfo]()

  private val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(if (networkConfig.testnet) Constants.TestnetPrefix else Constants.TestnetPrefix)

  private def findCoinbase(list: NonEmptyList[ApiTransaction]): ApiTransaction = list.last

  private def minerAddress(nfb: ApiFullBlock): String = {
    val tx = NonEmptyList.fromList(nfb.transactions.transactions).map(findCoinbase)
    tx.fold("unknown_miner") { t =>
      t.outputs
        .drop(1)
        .headOption
        .flatMap { o =>
          Base16.decode(o.proposition)
            .flatMap(bytes => addressEncoder.fromProposition(ValueSerializer.deserialize(bytes)))
            .map(_.toString)
            .toOption
        }
        .getOrElse("unknown_miner")
    }
  }

  private def minerRewardAndFee(nfb: ApiFullBlock): (Long, Long) = {
    val reward = CoinsEmission.emissionAtHeight(nfb.header.height)
    val tx = NonEmptyList.fromList(nfb.transactions.transactions).map(findCoinbase)
    val fee = tx.fold(0L) { t =>
      t.outputs
        .drop(1)
        .headOption
        .map(_.value - reward)
        .getOrElse(0L)
    }
    (reward, fee)
  }

  def extractBlockInfo(nfb: ApiFullBlock): BlockInfoWriter.ToInsert = {

    val (reward, fee) = minerRewardAndFee(nfb)
    val coinBaseValue = reward + fee
    val blockCoins = nfb.transactions.transactions.flatMap(_.outputs).map(_.value).sum - coinBaseValue
    val mAddress = minerAddress(nfb)
    val blockInfo = if (nfb.header.height == Constants.GenesisHeight) {
      BlockInfo(
        nfb.header.id,
        nfb.header.timestamp,
        nfb.header.height,
        nfb.header.difficulty.value.toLong,
        nfb.size,
        blockCoins,
        0L,
        nfb.transactions.transactions.length.toLong,
        nfb.transactions.transactions.map(_.size).sum,
        mAddress,
        reward,
        reward + fee,
        fee,
        nfb.size,
        nfb.transactions.transactions.length.toLong,
        CoinsEmission.issuedCoinsAfterHeight(nfb.header.height),
        0L,
        fee,
        reward,
        blockCoins
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
        nfb.size,
        blockCoins,
        nfb.header.timestamp - prev.timestamp,
        nfb.transactions.transactions.length.toLong,
        nfb.transactions.transactions.map(_.size).sum,
        mAddress,
        reward,
        reward + fee,
        fee,
        prev.blockChainTotalSize + nfb.size,
        nfb.transactions.transactions.length.toLong + prev.totalTxsCount,
        CoinsEmission.issuedCoinsAfterHeight(nfb.header.height),
        prev.totalMiningTime + miningTime,
        prev.totalFees + fee,
        prev.totalMinersReward + reward,
        prev.totalCoinsInTxs + blockCoins
      )
    }
    logger.trace(s"Putting block info for height ${blockInfo.height} into cache.")
    blockInfoCache.put(blockInfo.headerId, blockInfo)
    blockInfo
  }

}
