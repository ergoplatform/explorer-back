package org.ergoplatform.explorer.grabber.db

import java.math.BigInteger

import org.ergoplatform.explorer.config.NetworkConfig
import org.ergoplatform.explorer.grabber.Constants
import org.ergoplatform.{ErgoAddressEncoder, P2PKAddress, Pay2SAddress, Pay2SHAddress}
import org.scalatest.{Matchers, PropSpec, TryValues}
import scapi.sigma.DLogProtocol
import scapi.sigma.DLogProtocol.DLogProverInput
import sigmastate.Values
import sigmastate.serialization.ValueSerializer

class DBHelperTest extends PropSpec with Matchers with TryValues {

  property("scriptToAddress() should properly distinct all types of addresses from plain script bytes") {

    implicit val encoder: ErgoAddressEncoder = ErgoAddressEncoder(Constants.testnetPrefix)

    val dBHelper = new DBHelper(NetworkConfig(testnet = true))

    val pk: DLogProtocol.ProveDlog = DLogProverInput(BigInteger.ONE).publicImage
    val sh: Array[Byte] = ErgoAddressEncoder.hash192(ValueSerializer.serialize(pk))

    val p2s: Pay2SAddress = Pay2SAddress(Values.TrueLeaf)
    val p2sh: Pay2SHAddress = new Pay2SHAddress(sh)
    val p2pk: P2PKAddress = P2PKAddress(pk)

    dBHelper.scriptToAddress(p2s.scriptBytes).success.value.isInstanceOf[Pay2SAddress] shouldBe true
    dBHelper.scriptToAddress(p2sh.script.bytes).success.value.isInstanceOf[Pay2SHAddress] shouldBe true
    dBHelper.scriptToAddress(p2pk.contentBytes).success.value.isInstanceOf[P2PKAddress] shouldBe true
  }
}
