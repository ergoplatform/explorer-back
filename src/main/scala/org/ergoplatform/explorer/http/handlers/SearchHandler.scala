package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.IO
import org.ergoplatform.explorer.http.protocol.SearchInfo
import org.ergoplatform.explorer.services.{
  AddressesService,
  BlockService,
  MinerService,
  TransactionsService
}

final class SearchHandler(
  blockService: BlockService[IO],
  transactionService: TransactionsService[IO],
  addressService: AddressesService[IO],
  minerService: MinerService[IO]
) extends RouteHandler {

  val route: Route = (pathPrefix("search") & parameters("query".as[String])) { query =>
    validate(query.length >= 5, "'query' param should be at least 5 characters long") {
      for {
        blocks         <- blockService.searchById(query)
        transactions   <- transactionService.searchByIdSubstr(query)
        addresses      <- addressService.searchById(query)
        minerAddresses <- minerService.searchAddress(query)
      } yield SearchInfo(blocks, transactions, (addresses ++ minerAddresses).distinct)
    }
  }

}
