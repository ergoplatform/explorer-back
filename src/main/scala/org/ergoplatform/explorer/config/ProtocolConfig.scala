package org.ergoplatform.explorer.config

import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.explorer.Constants
import org.ergoplatform.mining.emission.EmissionRules
import org.ergoplatform.settings.MonetarySettings

final case class ProtocolConfig(testnet: Boolean, monetary: MonetarySettings) {

  val emission = new EmissionRules(monetary)

  val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(if (testnet) Constants.TestnetPrefix else Constants.MainnetPrefix)
}
