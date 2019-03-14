package org.ergoplatform.explorer.grabber

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.scalalogging.Logger
import org.bouncycastle.math.ec.custom.djb.Curve25519Point
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.NetworkConfig
import org.ergoplatform.explorer.db.models.BlockInfo
import org.ergoplatform.explorer.grabber.db.BlockInfoWriter
import org.ergoplatform.explorer.grabber.protocol.ApiFullBlock
import org.ergoplatform.{ErgoAddressEncoder, P2PKAddress}
import scorex.util.encode.Base16
import sigmastate.basics.DLogProtocol.ProveDlog
import sigmastate.interpreter.CryptoConstants
import sigmastate.serialization.{GroupElementSerializer, SigmaSerializer}

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

  private def minerAddress(nfb: ApiFullBlock): String = {
    Base16.decode(nfb.header.minerPk).flatMap { bytes =>
      Try(GroupElementSerializer.parse(SigmaSerializer.startReader(bytes)))
    } match {
      case scala.util.Success(x: CryptoConstants.EcPointType) => P2PKAddress(ProveDlog(x))(addressEncoder).toString
      case _ => throw new Exception("Failed to decode miner pk")
    }
  }

  private def minerRewardAndFee(nfb: ApiFullBlock): (Long, Long) = {
    val reward = CoinsEmission.emissionAtHeight(nfb.header.height)
    val fee = nfb.transactions.transactions
      .flatMap(_.outputs)
      .filter(_.proposition == Constants.FeePropositionScriptHex)
      .map(_.value)
      .sum
    (reward, fee)
  }

  def extractBlockInfo(nfb: ApiFullBlock): BlockInfoWriter.ToInsert = {
    val (reward, fee) = minerRewardAndFee(nfb)
    val coinBaseValue = reward + fee
    val blockCoins = nfb.transactions.transactions.flatMap(_.outputs).map(_.value).sum - coinBaseValue
    val mAddress = minerAddress(nfb)
    val blockInfo = if (nfb.header.height == Constants.GenesisHeight) {
      BlockInfo(
        headerId = nfb.header.id,
        timestamp = nfb.header.timestamp,
        height = nfb.header.height,
        difficulty = nfb.header.difficulty.value.toLong,
        blockSize = nfb.size,
        blockCoins = blockCoins,
        blockMiningTime = 0L,
        txsCount = nfb.transactions.transactions.length.toLong,
        txsSize = nfb.transactions.transactions.map(_.size).sum,
        minerAddress = mAddress,
        minerReward = reward,
        minerRevenue = reward + fee,
        blockFee = fee,
        blockChainTotalSize = nfb.size,
        totalTxsCount = nfb.transactions.transactions.length.toLong,
        totalCoinsIssued = CoinsEmission.issuedCoinsAfterHeight(nfb.header.height),
        totalMiningTime = 0L,
        totalFees = fee,
        totalMinersReward = reward,
        totalCoinsInTxs = blockCoins
      )
    } else {
      logger.trace("Getting prev blockInfo from cache.")
      val prev = blockInfoCache.getIfPresent(nfb.header.parentId).get
      val miningTime = nfb.header.timestamp - prev.timestamp

      BlockInfo(
        headerId = nfb.header.id,
        timestamp = nfb.header.timestamp,
        height = nfb.header.height,
        difficulty = nfb.header.difficulty.value.toLong,
        blockSize = nfb.size,
        blockCoins = blockCoins,
        blockMiningTime = nfb.header.timestamp - prev.timestamp,
        txsCount = nfb.transactions.transactions.length.toLong,
        txsSize = nfb.transactions.transactions.map(_.size).sum,
        minerAddress = mAddress,
        minerReward = reward,
        minerRevenue = reward + fee,
        blockFee = fee,
        blockChainTotalSize = prev.blockChainTotalSize + nfb.size,
        totalTxsCount = nfb.transactions.transactions.length.toLong + prev.totalTxsCount,
        totalCoinsIssued = CoinsEmission.issuedCoinsAfterHeight(nfb.header.height),
        totalMiningTime = prev.totalMiningTime + miningTime,
        totalFees = prev.totalFees + fee,
        totalMinersReward = prev.totalMinersReward + reward,
        totalCoinsInTxs = prev.totalCoinsInTxs + blockCoins
      )
    }
    logger.trace(s"Putting block info for height ${blockInfo.height} into cache.")
    blockInfoCache.put(blockInfo.headerId, blockInfo)
    blockInfo
  }

}
