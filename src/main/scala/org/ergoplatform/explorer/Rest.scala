package org.ergoplatform.explorer

import akka.http.scaladsl.server.Directives._
import org.ergoplatform.explorer.http.handlers._

trait Rest { self: Services =>

  val routes = List(
    new BlocksHandler(blocksService).route,
    new TransactionsHandler(txService).route,
    new AddressesHandler(addressesService, txService).route,
    new StatsHandler(statsService).route,
    new ChartsHandler(statsService).route,
    new InfoHandler(statsService).route,
    new SearchHandler(blocksService, txService, addressesService, minerService).route
  ).reduce(_ ~ _)
}
