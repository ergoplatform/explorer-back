package org.ergoplatform.explorer

import org.ergoplatform.explorer.config.ExplorerConfig
import pureconfig._

object Explorer extends App {

  val cfg = loadConfigOrThrow[ExplorerConfig]
  println(cfg)

}
