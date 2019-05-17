package org.ergoplatform.explorer.http

import akka.http.scaladsl.server.Route
import org.ergoplatform.explorer.Services
import org.ergoplatform.explorer.http.handlers._

trait Rest extends CorsHandler { self: Services =>

  val routes: Route = corsHandler(handlers.reduce(_ ~ _))

  private def handlers = List(
    new BlocksHandler(blocksService).route,
    new TransactionsHandler(txService).route,
    new AddressesHandler(addressesService, txService).route,
    new StatsHandler(statsService).route,
    new ChartsHandler(statsService).route,
    new InfoHandler(statsService).route,
    new SearchHandler(blocksService, txService, addressesService, minerService).route
  )

}
