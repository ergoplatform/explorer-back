package org.ergoplatform.explorer.services

import java.io.InputStream

import cats._
import cats.data.NonEmptyList
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import io.circe.syntax._
import io.circe.{Json, ParsingFailure}
import org.ergoplatform.explorer.config.GrabberConfig
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.http.protocol.{OutputInfo, TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.utils.Paging
import scalaj.http.Http

import scala.concurrent.ExecutionContext
import scala.io.Source

trait TransactionsService[F[_]] {

  def getTxInfo(id: String):  F[TransactionSummaryInfo]

  def getUnconfirmedTxInfo(id: String): F[ApiTransaction]

  def getUnconfirmed: F[List[ApiTransaction]]

  def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]]

  def countTxsByAddressId(addressId: String): F[Long]

  def searchById(query: String): F[List[String]]

  def getOutputById(id: String): F[OutputInfo]

  def getOutputsByAddress(address: String, unspentOnly: Boolean = false): F[List[OutputInfo]]

  def getOutputsByErgoTree(ergoTree: String, unspentOnly: Boolean = false): F[List[OutputInfo]]

  def submitTransaction(tx: Json): F[Json]

}

class TransactionsServiceIOImpl[F[_]](xa: Transactor[F],
                                      txPoolRef: Ref[F, TransactionsPool],
                                      ec: ExecutionContext,
                                      cfg: GrabberConfig)
                                     (implicit F: Monad[F],
                                      A: Async[F],
                                      M: MonadError[F, Throwable])
  extends TransactionsService[F] {

  val headersDao = new HeadersDao

  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao

  override def getUnconfirmedTxInfo(id: String): F[ApiTransaction] =
    txPoolRef.get.flatMap { _
      .get(id)
      .fold[F[ApiTransaction]](
        M.raiseError(new NoSuchElementException(s"Cannot find unconfirmed transaction with id = $id")))(F.pure)
    }

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

  override def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]] = for {
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

  override def countTxsByAddressId(addressId: String): F[Long] = for {
    _ <- Async.shift[F](ec)
    result <- getTxsCountByAddressIdResult(addressId)
  } yield result

  private def getTxsCountByAddressIdResult(addressId: String): F[Long] =
    transactionsDao.countTxsByAddressId(addressId).transact(xa)

  /** Search transaction identifiers by the fragment of the identifier
    */
  override def searchById(substring: String): F[List[String]] =
    transactionsDao.searchById(substring).transact(xa)

  override def getOutputById(id: String): F[OutputInfo] = outputDao.findByBoxId(id).transact(xa)
    .map(OutputInfo.fromOutputWithSpent)

  override def getOutputsByAddress(hash: String, unspentOnly: Boolean): F[List[OutputInfo]] = {
    (if (unspentOnly) outputDao.findUnspentByAddress(hash)
    else outputDao.findAllByAddress(hash))
      .transact(xa)
      .map(_.map(OutputInfo.fromOutputWithSpent))
  }

  override def getOutputsByErgoTree(proposition: String, unspentOnly: Boolean): F[List[OutputInfo]] = {
    (if (unspentOnly) outputDao.findUnspentByErgoTree(proposition)
    else outputDao.findAllByErgoTree(proposition))
      .transact(xa)
      .map(_.map(OutputInfo.fromOutputWithSpent))
  }

  override def submitTransaction(tx: Json): F[Json] = cfg.nodes
    .map { url =>
      F.pure(Http(s"$url/transactions").postData(tx.noSpaces).header("content-type", "application/json"))
        .flatMap(_.exec(requestParser).body)
    }
    .headOption
    .getOrElse(M.raiseError(new Exception("No known nodes responded")))

  override def getUnconfirmed: F[List[ApiTransaction]] = txPoolRef.get.map(_.getAll)

  private val requestParser: (Int, Map[String, IndexedSeq[String]], InputStream) => F[Json] =
    (code, _, is) => code match {
      case 200 =>
        val txId = Source.fromInputStream(is, "UTF8").mkString
        F.pure(Json.obj("id" -> txId.asJson))
      case 400 =>
        val str = Source.fromInputStream(is, "UTF8").mkString
        io.circe.parser.parse(str) match {
          case Right(json) => F.pure(json)
          case Left(pf: ParsingFailure) => M.raiseError(pf.underlying)
        }
      case _ =>
        val msg = Source.fromInputStream(is, "UTF8").mkString
        M.raiseError(
          new IllegalStateException(s"Request has been failed with code $code, and message $msg")
        )
    }

}
