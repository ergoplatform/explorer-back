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
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.explorer.Utils
import org.ergoplatform.explorer.config.ExplorerConfig
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.models.{Asset, Transaction}
import org.ergoplatform.explorer.db.models.composite.ExtendedOutput
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.http.protocol.{OutputInfo, TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.utils.Paging
import scalaj.http.Http
import scorex.util.encode.Base16
import sigmastate.Values.ErgoTree

import scala.concurrent.ExecutionContext
import scala.io.Source

trait TransactionsService[F[_]] {

  def getTxInfo(id: String): F[TransactionSummaryInfo]

  def getUnconfirmedTxInfo(id: String): F[ApiTransaction]

  def getUnconfirmed: F[List[ApiTransaction]]

  def getUnconfirmedByAddress(address: String): F[List[ApiTransaction]]

  def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]]

  def countTxsByAddressId(addressId: String): F[Long]

  def searchByIdSubstr(substring: String): F[List[String]]

  def getOutputById(id: String): F[OutputInfo]

  def getOutputsByAddress(address: String, unspentOnly: Boolean = false): F[List[OutputInfo]]

  def getOutputsByErgoTree(ergoTree: String, unspentOnly: Boolean = false): F[List[OutputInfo]]

  def submitTransaction(tx: Json): F[Json]

}

final class TransactionsServiceImpl[F[_]](
  xa: Transactor[F],
  txPoolRef: Ref[F, TransactionsPool],
  ec: ExecutionContext,
  cfg: ExplorerConfig
)(implicit F: Monad[F], A: Async[F], M: MonadError[F, Throwable])
  extends TransactionsService[F] {

  implicit val addressEncoder: ErgoAddressEncoder = cfg.protocol.addressEncoder

  val headersDao = new HeadersDao
  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao
  val assetsDao = new AssetsDao

  override def getUnconfirmedTxInfo(id: String): F[ApiTransaction] =
    txPoolRef.get.flatMap {
      _.get(id)
        .fold[F[ApiTransaction]](
          M.raiseError(
            new NoSuchElementException(s"Cannot find unconfirmed transaction with id = $id")
          )
        )(F.pure)
    }

  override def getTxInfo(id: String): F[TransactionSummaryInfo] =
    for {
      _      <- Async.shift[F](ec)
      result <- getTxInfoResult(id)
    } yield result

  override def getTxsByAddressId(addressId: String, p: Paging): F[List[TransactionInfo]] =
    for {
      _      <- Async.shift[F](ec)
      result <- getTxsByAddressIdResult(addressId, p)
    } yield result

  override def countTxsByAddressId(addressId: String): F[Long] =
    for {
      _      <- Async.shift[F](ec)
      result <- getTxsCountByAddressIdResult(addressId)
    } yield result

  override def searchByIdSubstr(substring: String): F[List[String]] =
    transactionsDao.searchById(substring).transact(xa)

  override def getOutputById(id: String): F[OutputInfo] = {
    val txn = for {
      out    <- outputDao.findByBoxId(id)
      assets <- assetsDao.getByBoxId(id)
    } yield OutputInfo(out, assets)
    txn.transact(xa)
  }

  override def getOutputsByAddress(
    address: String,
    unspentOnly: Boolean
  ): F[List[OutputInfo]] = {
    val txn = for {
      outputs <- if (unspentOnly) outputDao.findUnspentByAddress(address)
                 else outputDao.findAllByAddress(address)
      outputsWithAssets <- outputs
        .map(out => assetsDao.getByBoxId(out.output.boxId).map(out -> _))
        .sequence
      outputsInfo = outputsWithAssets.map { case (outs, assets) => OutputInfo(outs, assets) }
    } yield outputsInfo
    txn.transact(xa)
  }

  override def getOutputsByErgoTree(
    ergoTree: String,
    unspentOnly: Boolean
  ): F[List[OutputInfo]] = {
    val txn = for {
      outputs <- if (unspentOnly) outputDao.findUnspentByErgoTree(ergoTree)
                 else outputDao.findAllByErgoTree(ergoTree)
      outputsWithAssets <- outputs
        .map(out => assetsDao.getByBoxId(out.output.boxId).map(out -> _))
        .sequence
      outputsInfo = outputsWithAssets.map { case (outs, assets) => OutputInfo(outs, assets) }
    } yield outputsInfo
    txn.transact(xa)
  }

  override def submitTransaction(tx: Json): F[Json] =
    cfg.grabber.nodes
      .map { url =>
        F.pure(
            Http(s"$url/transactions")
              .postData(tx.noSpaces)
              .header("content-type", "application/json")
          )
          .flatMap(_.exec(requestParser).body)
      }
      .headOption
      .getOrElse(M.raiseError(new Exception("No known nodes responded")))

  override def getUnconfirmed: F[List[ApiTransaction]] = txPoolRef.get.map(_.getAll)

  override def getUnconfirmedByAddress(address: String): F[List[ApiTransaction]] =
    Utils
      .addressToErgoTree(address)
      .fold[F[ErgoTree]](M.raiseError, F.pure)
      .flatMap { tree =>
        txPoolRef.get.map(_.getByErgoTree(Base16.encode(tree.bytes)))
      }

  private def getOutputsWithAssetsByTxId(
    id: String
  ): ConnectionIO[List[(ExtendedOutput, List[Asset])]] =
    for {
      outputs <- outputDao.findAllByTxIdExtended(id)
      outputsWithAssets <- outputs
        .map(out => assetsDao.getByBoxId(out.output.boxId).map(out -> _))
        .sequence
    } yield outputsWithAssets

  private def getTxInfoResult(id: String): F[TransactionSummaryInfo] =
    (for {
      tx            <- transactionsDao.get(id)
      is            <- inputDao.findAllByTxIdExtended(tx.id)
      os            <- getOutputsWithAssetsByTxId(tx.id)
      h             <- headersDao.getHeightById(tx.headerId)
      currentHeight <- headersDao.getLast(1).map(_.headOption.map(_.height).getOrElse(0L))
      info = TransactionSummaryInfo(tx, h, currentHeight - h, is, os)
    } yield info).transact(xa)

  private def getTxsByAddressIdResult(addressId: String, p: Paging): F[List[TransactionInfo]] =
    (for {
      txs     <- transactionsDao.getTxsByAddressId(addressId, p.offset, p.limit)
      txsInfo <- txs.map(getTxInfoByTransactions).sequence
    } yield txsInfo).transact(xa)

  private def getTxInfoByTransactions(tx: Transaction): ConnectionIO[TransactionInfo] =
    for {
      currentHeight <- headersDao.getLast(1).map(_.headOption.map(_.height).getOrElse(0L))
      txHeight      <- transactionsDao.txHeight(tx.id)
      is            <- inputDao.findAllByTxIdExtended(tx.id)
      os            <- getOutputsWithAssetsByTxId(tx.id)
      confirmations = currentHeight - txHeight + 1
    } yield TransactionInfo.apply(tx, confirmations, is, os)

  private def getTxsCountByAddressIdResult(addressId: String): F[Long] =
    transactionsDao.countTxsByAddressId(addressId).transact(xa)

  private val requestParser: (Int, Map[String, IndexedSeq[String]], InputStream) => F[Json] =
    (code, _, is) =>
      code match {
        case 200 =>
          val txId = Source.fromInputStream(is, "UTF8").mkString
          F.pure(Json.obj("id" -> txId.asJson))
        case 400 =>
          val str = Source.fromInputStream(is, "UTF8").mkString
          io.circe.parser.parse(str) match {
            case Right(json)              => F.pure(json)
            case Left(pf: ParsingFailure) => M.raiseError(pf.underlying)
          }
        case _ =>
          val msg = Source.fromInputStream(is, "UTF8").mkString
          M.raiseError(
            new IllegalStateException(s"Request has been failed with code $code, and message $msg")
          )
    }

}
