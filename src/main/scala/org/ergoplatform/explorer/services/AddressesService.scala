package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.{AddressDao, OutputsDao}
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
  val addressDao = new AddressDao

  override def getAddressInfo(addressId: String): F[AddressInfo] = for {
    _ <- Async.shift[F](ec)
    info <- getAddressInfoResult(addressId)
  } yield info

  private def getAddressInfoResult(addressId: String): F[AddressInfo] = addressDao
    .getAddressData(addressId)
    .map(AddressInfo.apply)
    .transact(xa)

  def searchById(substring: String): F[List[String]] = {
    outputsDao.searchByAddressId(substring).transact(xa)
  }

}