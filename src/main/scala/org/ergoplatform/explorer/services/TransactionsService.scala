package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.http.protocol.TransactionSummaryInfo
import org.ergoplatform.explorer.utils.Converter._

import scala.concurrent.ExecutionContext

trait TransactionsService[F[_]] {

  def getTxInfo(id: String):  F[TransactionSummaryInfo]

}

class TransactionsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                                     (implicit F: Monad[F], A: Async[F]) extends TransactionsService[F] {

  val headersDao = new HeadersDao
  val interlinksDao = new InterlinksDao
  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao


  override def getTxInfo(id: String): F[TransactionSummaryInfo] = for {
    _ <- Async.shift[F](ec)
    base16Id <- F.pure(from58to16(id))
    result <- getTxInfoResult(base16Id)
  } yield result

  private def getTxInfoResult(id: String): F[TransactionSummaryInfo] = (for {
    tx <- transactionsDao.get(id)
    h <- headersDao.getHeightById(tx.blockId)
    info = TransactionSummaryInfo.fromDb(tx, h)
  } yield info).transact(xa)

}
