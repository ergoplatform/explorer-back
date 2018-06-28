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

  val outputsDao = new OutputsDao

  override def getAddressInfo(addressId: String): F[AddressInfo] = for {
    _ <- Async.shift[F](ec)
    info <- getAddressInfoResultWithFilter(addressId)
  } yield info

  private def getAddressInfoResultWithFilter(addressId: String): F[AddressInfo] = A.suspend {
    if (addressId.startsWith("cd0703")) {
      getAddressInfoResult(addressId)
    } else {
      F.pure(AddressInfo(addressId, List.empty))
    }
  }

  private def getAddressInfoResult(addressId: String): F[AddressInfo] = outputsDao
    .findAllByAddressId(addressId)
    .map { os => AddressInfo.apply(addressId, os) }
    .transact(xa)

  def searchById(substring: String): F[List[String]] = {
    //https://github.com/ergoplatform/explorer-back/issues/5
    if (substring.startsWith("cd0703")) {
      outputsDao.searchByAddressId(substring).transact(xa)
    } else {
      F.pure(List.empty)
    }
  }

}
