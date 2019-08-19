package org.ergoplatform.explorer.config

import org.ergoplatform.settings.MonetarySettings
import scala.concurrent.duration._

trait Config {

  val grabberConfig = GrabberConfig(List("http://127.0.0.1"), 10.seconds, 5.seconds)

  val dbConfig = DbConfig()

  val httpConfig = HttpConfig()

  val protocolConfig = ProtocolConfig(testnet = true, MonetarySettings())

  val cfg = ExplorerConfig(dbConfig, httpConfig, grabberConfig, protocolConfig)

}
