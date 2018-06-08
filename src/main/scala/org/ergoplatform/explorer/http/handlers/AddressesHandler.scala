package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import cats.syntax.all._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.http.protocol.ItemsResponse
import org.ergoplatform.explorer.services.{AddressesService, TransactionsService}
import org.ergoplatform.explorer.utils.Paging

class AddressesHandler(as: AddressesService[IO], ts: TransactionsService[IO]) extends FailFastCirceSupport
  with CommonDirectives {

  val route = pathPrefix("addresses") {
    getTxsByAddressId ~ getAddressById
  }

  val getAddressById = (get & base16Segment) { id =>
    val f = as.getAddressInfo(id).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

  val getTxsByAddressId = (get & path(Segment / "transactions" ) & paging) { (addressId, o, l) =>
    val p = Paging(offset = o, limit = l)
    val items = ts.getTxsByAddressId(addressId, p)
    val count = ts.countTxsByAddressId(addressId)
    val f = (items, count).parMapN((i, c) => ItemsResponse(i, c)).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }
}
