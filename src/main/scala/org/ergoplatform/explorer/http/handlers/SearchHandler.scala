package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.services.{AddressesService, BlockService, TransactionsService}

class SearchHandler(blockService: BlockService[IO],
                    transactionsService: TransactionsService[IO],
                    addressService: AddressesService[IO]) extends ApiRoute {

  val route = (pathPrefix("search") & parameters("query".as[String])) { query =>
    validate(query.length >= 5, "'query' param should be at least 5 characters long") {
      for {
        blocks <- blockService.searchById(query)
        transactions <- transactionsService.searchById(query)
        addresses <- addressService.searchById(query)
      } yield Json.obj(
        "blocks" -> blocks.asJson,
        "transactions" -> transactions.asJson,
        "addresses" -> addresses.asJson
      )
    }
  }

}
