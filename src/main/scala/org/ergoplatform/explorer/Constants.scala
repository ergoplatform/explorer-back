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

  val EmissionScriptHex: String =
    "4FHL3zghhZXXKom5gVKhxCyLxGXFUBjohXS7WNdwwarKbg8PnGyJJ3NUnebQEpSRYWUkfmHUnPVpStvMHpj6oRhrHo8xLC9zjS4a5sbrSSopKgZx" +
      "pgPQd9SXiQiMnvvGqtAaA3eybnC1KdJL9oPvjj4x7oeuteH3UPZzTdC4KSJH1JGM87hwg7husYu1GFMwidz7UEZDJjj5WzzUTdvUqjnuaC84JN" +
      "JC4DiKJhWc4iept9ZK2x8EHZnEWhy1j43ER1rdb6VCTj3z69Gxz6kbLwhibachJRo8myTM95KTgFvyx9MD3vLafAs4Sj3dq6DiB"

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

  def rewardOutputScriptConstantBytes: Array[Byte] = {
    rewardOutputScript(MinerRewardDelta, ProveDlog(group.generator)).bytes.dropRight(PublicKeyLength)
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
