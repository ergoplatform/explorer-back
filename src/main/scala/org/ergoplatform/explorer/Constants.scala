package org.ergoplatform.explorer

import org.ergoplatform._
import scorex.util.encode.Base16
import sigmastate.basics.BcDlogGroup
import sigmastate.interpreter.CryptoConstants
import sigmastate.interpreter.CryptoConstants.EcPointType

object Constants {

  val TestnetPrefix: Byte = 0x10
  val MainnetPrefix: Byte = 0x00
  val PreGenesisHeight: Long = 0L
  val GenesisHeight: Long = PreGenesisHeight + 1

  val PublicKeyLength = 33

  val EpochLength = 1024

  val MinerRewardDelta = 720

  val TeamTreasuryThreshold = 67500000000L

  val group: BcDlogGroup[EcPointType] = CryptoConstants.dlogGroup

  val FeePropositionScriptHex: String = {
    val script = ErgoScriptPredef.feeProposition(MinerRewardDelta)
    Base16.encode(script.bytes)
  }

}
