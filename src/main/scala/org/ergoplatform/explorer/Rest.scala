package org.ergoplatform.explorer

import org.ergoplatform.explorer.http.handlers.BlocksHandler

trait Rest { self: Services =>

  val routes = new BlocksHandler(blocksService).route
}
