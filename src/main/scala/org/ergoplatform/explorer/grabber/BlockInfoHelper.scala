package org.ergoplatform.explorer.grabber

import com.github.blemale.scaffeine.{Cache, Scaffeine}
import com.typesafe.scalalogging.Logger
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ProtocolConfig
import org.ergoplatform.explorer.db.models.BlockInfo
import org.ergoplatform.explorer.grabber.db.BlockInfoWriter
import org.ergoplatform.explorer.grabber.protocol.ApiFullBlock
import org.ergoplatform.{ErgoAddressEncoder, ErgoScriptPredef, Pay2SAddress}
import scorex.util.encode.Base16
import sigmastate.basics.DLogProtocol.ProveDlog
import sigmastate.interpreter.CryptoConstants
import sigmastate.serialization.{GroupElementSerializer, SigmaSerializer}

import scala.concurrent.duration._
import scala.util.Try

class BlockInfoHelper(protocolConfig: ProtocolConfig) {

  val logger = Logger("blocks-info-helper")

  val blockInfoCache: Cache[String, BlockInfo] = Scaffeine()
    .recordStats()
    .expireAfterWrite(10 minutes)
    .maximumSize(1000)
    .build[String, BlockInfo]()

  private val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(
      if (protocolConfig.testnet) Constants.TestnetPrefix else Constants.MainnetPrefix
    )

  private def minerRewardAddress(nfb: ApiFullBlock): String =
    Base16.decode(nfb.header.minerPk).flatMap { bytes =>
      Try(GroupElementSerializer.parse(SigmaSerializer.startReader(bytes)))
    } match {
      case scala.util.Success(x: CryptoConstants.EcPointType) =>
        val minerPk = ProveDlog(x)
        val rewardScript =
          ErgoScriptPredef.rewardOutputScript(protocolConfig.monetary.minerRewardDelay, minerPk)
        Pay2SAddress(rewardScript)(addressEncoder).toString
      case _ => throw new Exception("Failed to decode miner pk")
    }

  private def minerRewardAndFee(nfb: ApiFullBlock): (Long, Long) = {
    val emission = protocolConfig.emission.emissionAtHeight(nfb.header.height)
    val reward = math.min(Constants.TeamTreasuryThreshold, emission)
    val fee = nfb.transactions.transactions
      .flatMap(_.outputs)
      .filter(_.ergoTree == Constants.FeePropositionScriptHex)
      .map(_.value)
      .sum
    (reward, fee)
  }

  def assembleGenesisInfo(nfb: ApiFullBlock): BlockInfoWriter.ToInsert = {
    val (reward, fee) = minerRewardAndFee(nfb)
    val coinBaseValue = reward + fee
    val blockCoins = nfb.transactions.transactions
      .flatMap(_.outputs)
      .map(_.value)
      .sum - coinBaseValue
    val mAddress = minerRewardAddress(nfb)

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
      totalCoinsIssued = protocolConfig.emission.issuedCoinsAfterHeight(nfb.header.height),
      totalMiningTime = 0L,
      totalFees = fee,
      totalMinersReward = reward,
      totalCoinsInTxs = blockCoins
    )
  }

  def assembleNonGenesisInfo(
    nfb: ApiFullBlock,
    parentBlockInfo: BlockInfo
  ): BlockInfoWriter.ToInsert = {
    val (reward, fee) = minerRewardAndFee(nfb)
    val coinBaseValue = reward + fee
    val blockCoins = nfb.transactions.transactions
      .flatMap(_.outputs)
      .map(_.value)
      .sum - coinBaseValue
    val mAddress = minerRewardAddress(nfb)
    val miningTime = nfb.header.timestamp - parentBlockInfo.timestamp

    BlockInfo(
      headerId = nfb.header.id,
      timestamp = nfb.header.timestamp,
      height = nfb.header.height,
      difficulty = nfb.header.difficulty.value.toLong,
      blockSize = nfb.size,
      blockCoins = blockCoins,
      blockMiningTime = nfb.header.timestamp - parentBlockInfo.timestamp,
      txsCount = nfb.transactions.transactions.length.toLong,
      txsSize = nfb.transactions.transactions.map(_.size).sum,
      minerAddress = mAddress,
      minerReward = reward,
      minerRevenue = reward + fee,
      blockFee = fee,
      blockChainTotalSize = parentBlockInfo.blockChainTotalSize + nfb.size,
      totalTxsCount = nfb.transactions.transactions.length.toLong + parentBlockInfo.totalTxsCount,
      totalCoinsIssued = protocolConfig.emission.issuedCoinsAfterHeight(nfb.header.height),
      totalMiningTime = parentBlockInfo.totalMiningTime + miningTime,
      totalFees = parentBlockInfo.totalFees + fee,
      totalMinersReward = parentBlockInfo.totalMinersReward + reward,
      totalCoinsInTxs = parentBlockInfo.totalCoinsInTxs + blockCoins
    )
  }

}
