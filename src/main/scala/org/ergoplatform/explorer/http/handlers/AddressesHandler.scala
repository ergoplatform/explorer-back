package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.effect.{ContextShift, IO}
import cats.syntax.all._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.http.protocol.{ItemsResponse, TransactionInfo}
import org.ergoplatform.explorer.services.{AddressesService, TransactionsService}
import org.ergoplatform.explorer.utils.Paging

import scala.concurrent.ExecutionContext

class AddressesHandler(as: AddressesService[IO], ts: TransactionsService[IO])(
  implicit ec: ExecutionContext
) extends FailFastCirceSupport
    with CommonDirectives {

  implicit val contextShift: ContextShift[IO] = IO.contextShift(ec)

  val route: Route = pathPrefix("addresses") {
    getTxsByAddressId ~ getAddressById
  }

  def getAddressById: Route = (get & base58Segment) { id =>
    onSuccess(as.getAddressInfo(id).unsafeToFuture()) { complete(_) }
  }

  def getTxsByAddressId: Route = (get & path(Segment / "transactions") & paging) {
    (addressId, o, l) =>
      val items: IO[List[TransactionInfo]] =
        ts.getTxsByAddressId(addressId, Paging(offset = o, limit = l))
      val count: IO[Long] = ts.countTxsByAddressId(addressId)
      onSuccess((items, count).parMapN((i, c) => ItemsResponse(i, c)).unsafeToFuture()) {
        complete(_)
      }
  }

}
