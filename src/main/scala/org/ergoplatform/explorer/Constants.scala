package org.ergoplatform.explorer

import org.ergoplatform.{Height, MinerPubkey, Outputs, Self}
import scorex.util.encode.Base16
import sigmastate.SCollection.SByteArray
import sigmastate.Values.{IntArrayConstant, IntConstant, SigmaPropValue, Value}
import sigmastate.basics.BcDlogFp
import sigmastate.basics.DLogProtocol.ProveDlog
import sigmastate.interpreter.CryptoConstants
import sigmastate.interpreter.CryptoConstants.EcPointType
import sigmastate.utxo._
import sigmastate._
import sigmastate.lang.Terms._
import sigmastate.serialization.ErgoTreeSerializer

object Constants {

  val TestnetPrefix: Byte = 0x10
  val MainnetPrefix: Byte = 0x00
  val PreGenesisHeight: Long = 0L
  val GenesisHeight: Long = PreGenesisHeight + 1

  val PublicKeyLength = 33

  val MinerRewardDelta = 720

  val group: BcDlogFp[EcPointType] = CryptoConstants.dlogGroup

  val FeePropositionScriptHex: String= {
    val out = ByIndex(Outputs, IntConstant(0))
    val script = AND(
      EQ(Height, boxCreationHeight(out)),
      EQ(ExtractScriptBytes(out), expectedMinerOutScriptBytesVal(MinerRewardDelta, MinerPubkey)),
      EQ(SizeOf(Outputs), 1)
    )
    Base16.encode(script.bytes)
  }

  def rewardOutputScript(delta: Int, minerPk: ProveDlog): Value[SBoolean.type] = {
    AND(
      GE(Height, Plus(SelectField(ExtractCreationInfo(Self), 1).asIntValue, IntConstant(delta))),
      minerPk
    )
  }

  private def boxCreationHeight(box: Value[SBox.type]): Value[SInt.type] =
    SelectField(ExtractCreationInfo(box), 1).asIntValue

  private def expectedMinerOutScriptBytesVal(delta: Int, minerPkBytesVal: Value[SByteArray]): Value[SByteArray] = {
    val genericPk = ProveDlog(group.generator)
    val genericMinerProp = rewardOutputScript(delta, genericPk)
    val genericMinerPropBytes = ErgoTreeSerializer.DefaultSerializer.serializeWithSegregation(genericMinerProp)
    val expectedGenericMinerProp = AND(
      GE(Height, Plus(boxCreationHeight(Self), IntConstant(delta))),
      genericPk
    )
    assert(genericMinerProp == expectedGenericMinerProp)
    // first segregated constant is delta, so key is second constant
    val positions = IntArrayConstant(Array[Int](1))
    val minerPubkeySigmaProp = ProveDlog(DecodePoint(minerPkBytesVal))
    val newVals = Values.ConcreteCollection(Vector[SigmaPropValue](minerPubkeySigmaProp), SSigmaProp)
    SubstConstants(genericMinerPropBytes, positions, newVals)
  }

}
