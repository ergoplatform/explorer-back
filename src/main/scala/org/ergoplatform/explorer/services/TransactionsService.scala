package org.ergoplatform.explorer.services

import cats._
import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.http.protocol.{OutputInfo, TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.utils.Paging

import scala.concurrent.ExecutionContext

trait TransactionsService[F[_]] {

  def getTxInfo(id: String):  F[TransactionSummaryInfo]

  def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]]

  def countTxsByAddressId(addressId: String): F[Long]

  def searchById(query: String): F[List[String]]

  def getOutputsByHash(hash: String, unspentOnly: Boolean = false): F[List[OutputInfo]]

  def getOutputsByProposition(ergoTree: String, unspentOnly: Boolean = false): F[List[OutputInfo]]

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
    currentHeight <- headersDao.getLast(1).map(_.headOption.map(_.height).getOrElse(0L))
    confirmations <- if (ids.isEmpty) {
      List.empty[(String, Long)].pure[ConnectionIO]
    } else {
      transactionsDao.txsHeights(NonEmptyList.fromListUnsafe(ids))
        .map { list => list.map(v => v._1 -> (currentHeight - v._2 + 1L)) }
    }
    is <- inputDao.findAllByTxsIdWithValue(ids)
    os <- outputDao.findAllByTxsIdWithSpent(ids)
  } yield TransactionInfo.extractInfo(txs, confirmations, is, os)).transact(xa)

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

  def getOutputsByHash(hash: String, unspentOnly: Boolean): F[List[OutputInfo]] = {
    if (unspentOnly) outputDao.findUnspentByHash(hash).transact(xa)
      .map(_.map(OutputInfo.fromOutputWithSpent))
    else outputDao.findAllByHash(hash).transact(xa)
      .map(_.map(OutputInfo.fromOutput))
  }

  def getOutputsByProposition(proposition: String, unspentOnly: Boolean): F[List[OutputInfo]] = {
    if (unspentOnly) outputDao.findUnspentByProposition(proposition).transact(xa)
      .map(_.map(OutputInfo.fromOutputWithSpent))
    else outputDao.findAllByProposition(proposition).transact(xa)
      .map(_.map(OutputInfo.fromOutput))
  }

}
