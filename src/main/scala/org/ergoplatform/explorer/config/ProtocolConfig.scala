package org.ergoplatform.explorer.config

import org.ergoplatform.mining.emission.EmissionRules
import org.ergoplatform.settings.MonetarySettings

case class ProtocolConfig(testnet: Boolean, monetary: MonetarySettings) {

  val emission = new EmissionRules(monetary)
}
