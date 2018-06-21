package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.OutputsDao
import org.ergoplatform.explorer.http.protocol.AddressInfo

import scala.concurrent.ExecutionContext

trait AddressesService[F[_]] {

  def getAddressInfo(addressId: String): F[AddressInfo]

  def searchById(query: String): F[List[String]]

}

class AddressesServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                                  (implicit F: Monad[F], A: Async[F]) extends AddressesService[F] {

  val outputsDao = new OutputsDao

  override def getAddressInfo(addressId: String): F[AddressInfo] = for {
    _ <- Async.shift[F](ec)
    info <- getAddressInfoResult(addressId)
  } yield info

  private def getAddressInfoResult(addressId: String): F[AddressInfo] = outputsDao
    .findAllByAddressId(addressId)
    .map { os => AddressInfo.apply(addressId, os) }
    .transact(xa)

  def searchById(substring: String): F[List[String]] = {
    outputsDao.searchByAddressId(substring).transact(xa)
  }

}
