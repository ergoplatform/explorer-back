package org.ergoplatform.explorer

import akka.http.scaladsl.server.Directives._
import org.ergoplatform.explorer.http.handlers.{AddressesHandler, BlocksHandler, TransactionsHandler, UtilHandler}

trait Rest { self: Services =>

  val routes = List(
    new BlocksHandler(blocksService).route,
    new TransactionsHandler(txService).route,
    new AddressesHandler(addressesService, txService).route,
    new UtilHandler().route
  ).reduce(_ ~ _)
}
