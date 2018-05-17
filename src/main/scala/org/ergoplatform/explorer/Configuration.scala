package org.ergoplatform.explorer

import org.ergoplatform.explorer.config.ExplorerConfig
import pureconfig.loadConfigOrThrow

trait Configuration {
  val cfg: ExplorerConfig = loadConfigOrThrow[ExplorerConfig]
}
