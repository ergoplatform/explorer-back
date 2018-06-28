package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.OutputsDao
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.http.protocol.AddressInfo

import scala.concurrent.ExecutionContext

trait AddressesService[F[_]] {

  def getAddressInfo(addressId: String): F[AddressInfo]

  def searchById(query: String): F[List[String]]

}

class AddressesServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                                  (implicit F: Monad[F], A: Async[F]) extends AddressesService[F] with JsonMeta {
  import AddressesService._

  val outputsDao = new OutputsDao

  override def getAddressInfo(addressId: String): F[AddressInfo] = for {
    _ <- Async.shift[F](ec)
    info <- getAddressInfoFilteringNonStandard(addressId)
  } yield info

  private def getAddressInfoFilteringNonStandard(addressId: String): F[AddressInfo] = A.suspend {
    if (isStandardAddress(addressId)) {
      getAddressInfoResult(addressId)
    } else {
      F.pure(AddressInfo(addressId, List.empty))
    }
  }

  private def getAddressInfoResult(addressId: String): F[AddressInfo] = outputsDao
    .findAllByAddressId(addressId)
    .map { os => AddressInfo.apply(addressId, os) }
    .transact(xa)

  def searchById(substring: String): F[List[String]] = if (isStandardAddress(substring)) {
    outputsDao.searchByAddressId(substring).transact(xa)
  } else {
    F.pure(List.empty)
  }

}

object AddressesService {
  /**
    * Currently we are supporting only standart addresses, they all are starting with cb0703 char sequence
    *
    * More details https://github.com/ergoplatform/explorer-back/issues/5
    * @param address String representation of address
    * @return true or false
    */
  def isStandardAddress(address: String): Boolean = address.startsWith("cb0703")
}
