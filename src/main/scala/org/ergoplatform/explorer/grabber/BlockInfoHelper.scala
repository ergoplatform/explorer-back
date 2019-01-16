package org.ergoplatform.explorer.grabber

import cats.data.NonEmptyList
import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.scalalogging.Logger
import org.bouncycastle.math.ec.custom.djb.Curve25519Point
import org.ergoplatform.{ErgoAddressEncoder, P2PKAddress}
import org.ergoplatform.explorer.config.NetworkConfig
import org.ergoplatform.explorer.db.models.BlockInfo
import org.ergoplatform.explorer.grabber.db.BlockInfoWriter
import org.ergoplatform.explorer.grabber.protocol.{ApiFullBlock, ApiTransaction}
import scorex.util.encode.Base16
import sigmastate.basics.DLogProtocol.ProveDlog

import scala.concurrent.duration._
import scala.util.Try

class BlockInfoHelper(networkConfig: NetworkConfig) {

  val logger = Logger("blocks-info-helper")

  val blockInfoCache: Cache[String, BlockInfo] = Scaffeine()
    .recordStats()
    .expireAfterWrite(10 minutes)
    .maximumSize(1000)
    .build[String, BlockInfo]()

  private val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(if (networkConfig.testnet) Constants.TestnetPrefix else Constants.TestnetPrefix)

  private def findMinerTxs(txs: List[ApiTransaction]): List[ApiTransaction] = {
    txs.takeRight(2).filter(_.outputs.exists { out =>
      val rewardScript = Constants.rewardOutputScriptConstantBytes
      out.proposition.take(rewardScript.length * 2) == Base16.encode(rewardScript)
    })
  }

  private def minerAddress(nfb: ApiFullBlock): String = {
    Base16.decode(nfb.header.minerPk).flatMap(bytes => Try(Constants.group.curve.decodePoint(bytes))) match {
      case scala.util.Success(x: Curve25519Point) => P2PKAddress(ProveDlog(x))(addressEncoder).toString
      case _ => throw new Exception("Failed to decode miner pk")
    }
  }

  private def minerRewardAndFee(nfb: ApiFullBlock): (Long, Long) = {
    val reward = CoinsEmission.emissionAtHeight(nfb.header.height)
    val txsOpt = NonEmptyList.fromList(nfb.transactions.transactions).map(x => findMinerTxs(x.toList))
    val fee = if (txsOpt.exists(_.size == 2)) {
      txsOpt.fold(0L) {
        _.last
          .outputs
          .map(_.value)
          .sum
      }
    } else {
      0L
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
