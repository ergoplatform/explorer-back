package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao.StatsDao
import org.ergoplatform.explorer.http.protocol.StatsSummary

import scala.concurrent.ExecutionContext

trait StatsService[F[_]] {

  def findLastStats: F[Option[StatsSummary]]
}

class StatsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                        (implicit F: Monad[F], A: Async[F]) extends StatsService[F] {

  val statsDao = new StatsDao

  override def findLastStats: F[Option[StatsSummary]] = for {
    _ <- Async.shift[F](ec)
    result <- statsDao.findLast.map(_.map(StatsSummary.apply)).transact[F](xa)
  } yield result

}
