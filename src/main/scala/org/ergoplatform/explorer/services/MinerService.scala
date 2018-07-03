package org.ergoplatform.explorer.services

import cats.Monad
import cats.effect.Async
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.MinerDao

import scala.concurrent.ExecutionContext

trait MinerService[F[_]] {

  def searchAddress(query: String): F[List[String]]

}

class MinerServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                              (implicit F: Monad[F], A: Async[F]) extends MinerService[F] {

  val minerDao = new MinerDao

  /** Search address by the fragment of the address */
  def searchAddress(substring: String): F[List[String]] = {
    for {
      _ <- Async.shift[F](ec)
      addresses <- minerDao.searchAddress(substring).transact(xa)
    } yield addresses
  }

}
