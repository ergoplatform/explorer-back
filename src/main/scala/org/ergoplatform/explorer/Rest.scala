package org.ergoplatform.explorer

import akka.http.scaladsl.server.Directives._
import org.ergoplatform.explorer.http.handlers.{BlocksHandler, TransactionsHandler}

trait Rest { self: Services =>

  val routes = new BlocksHandler(blocksService).route ~ new TransactionsHandler(txService).route
}
