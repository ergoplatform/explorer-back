package org.ergoplatform.explorer

import akka.http.scaladsl.server.Directives._
import org.ergoplatform.explorer.http.handlers.{AddressesHandler, BlocksHandler, TransactionsHandler}

trait Rest { self: Services =>

  val routes = List(
    new BlocksHandler(blocksService).route,
    new TransactionsHandler(txService).route,
    new AddressesHandler(addressesService).route
  ).reduce(_ ~ _)
}
