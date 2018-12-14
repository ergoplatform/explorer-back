package org.ergoplatform.explorer.grabber

import org.ergoplatform.{Height, Self}
import scapi.sigma.BcDlogFp
import scapi.sigma.DLogProtocol.ProveDlog
import sigmastate.{AND, GE, Plus, SBoolean}
import sigmastate.Values.{LongConstant, Value}
import sigmastate.interpreter.CryptoConstants
import sigmastate.interpreter.CryptoConstants.EcPointType
import sigmastate.utxo.{ExtractCreationInfo, SelectField}
import sigmastate.lang.Terms._

object Constants {

  val TestnetPrefix: Byte = 0x10
  val MainnetPrefix: Byte = 0x00
  val PreGenesisHeight: Long = 0L
  val GenesisHeight: Long = PreGenesisHeight + 1

  val PublicKeyLength = 33

  val MinerRewardDelta = 720

  val group: BcDlogFp[EcPointType] = CryptoConstants.dlogGroup

  def rewardOutputScript(delta: Int, minerPk: ProveDlog): Value[SBoolean.type] = {
    AND(
      GE(Height, Plus(SelectField(ExtractCreationInfo(Self), 1).asLongValue, LongConstant(delta))),
      minerPk
    )
  }

  def rewardOutputScriptConstantBytes: Array[Byte] = {
    rewardOutputScript(MinerRewardDelta, ProveDlog(group.generator)).bytes.dropRight(PublicKeyLength)
  }

}
