package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.http.protocol.{TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.utils.Paging

import scala.concurrent.ExecutionContext

trait TransactionsService[F[_]] {

  def getTxInfo(id: String):  F[TransactionSummaryInfo]

  def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]]

  def countTxsByAddressId(addressId: String): F[Long]

  def searchById(query: String): F[List[String]]

}

class TransactionsServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                                     (implicit F: Monad[F], A: Async[F]) extends TransactionsService[F] {

  val headersDao = new HeadersDao
  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao


  override def getTxInfo(id: String): F[TransactionSummaryInfo] = for {
    _ <- Async.shift[F](ec)
    result <- getTxInfoResult(id)
  } yield result

  private def getTxInfoResult(id: String): F[TransactionSummaryInfo] = (for {
    tx <- transactionsDao.get(id)
    is <- inputDao.findAllByTxIdWithValue(tx.id)
    os <- outputDao.findAllByTxIdWithSpent(tx.id)
    h <- headersDao.getHeightById(tx.headerId)
    currentHeight <- headersDao.getLast(1).map(_.headOption.map(_.height).getOrElse(0L))
    info = TransactionSummaryInfo.fromDb(tx, h, currentHeight - h, is, os)
  } yield info).transact(xa)

  def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]] = for {
    _ <- Async.shift[F](ec)
    result <- getTxsByAddressIdResult(addressId, p)
  } yield result

  private def getTxsByAddressIdResult(addressId: String, p: Paging): F[List[TransactionInfo]] = (for {
    txs <- transactionsDao.getTxsByAddressId(addressId, p.offset, p.limit)
    ids = txs.map(_.id)
    is <- inputDao.findAllByTxsIdWithValue(ids)
    os <- outputDao.findAllByTxsIdWithSpent(ids)
  } yield TransactionInfo.extractInfo(txs, is ,os)).transact(xa)

  def countTxsByAddressId(addressId: String): F[Long] = for {
    _ <- Async.shift[F](ec)
    result <- getTxsCountByAddressIdResult(addressId)
  } yield result

  private def getTxsCountByAddressIdResult(addressId: String): F[Long] =
    transactionsDao.countTxsByAddressId(addressId).transact(xa)

  /** Search transaction identifiers by the fragment of the identifier */
  def searchById(substring: String): F[List[String]] = {
    transactionsDao.searchById(substring).transact(xa)
  }

}
