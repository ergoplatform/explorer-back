package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.http.protocol.FullBlockInfo
import scorex.crypto.encode.{Base16, Base58}

import scala.concurrent.ExecutionContext


trait BlockService[F[_]] {

  /**
    * @param id base58 representation of id byte array
    * @return fullBlockInfo
    */
  def getBlock(id: String): F[FullBlockInfo]
}

class BlocksServiceImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                             (implicit F: Monad[F], A: Async[F]) extends BlockService[F] {

  val headersDao = new HeadersDao
  val interlinksDao = new InterlinksDao
  val transactionsDap = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao

  private def base58ToBase16(base58: String): String = Base16.encode(Base58.decode(base58).get)

  override def getBlock(id: String): F[FullBlockInfo] = for {
    _ <- Async.shift[F](ec)
    base16Id <- F.pure(base58ToBase16(id))
    result <- getBlockResult(base16Id)
  } yield result

  private def getBlockResult(id: String): F[FullBlockInfo] = (for {
    h <- headersDao.get(id)
    links <- interlinksDao.findAllByBLockId(h.id)
    txs <- transactionsDap.findAllByBLockId(h.id)
    txsIds = txs.map(_.id)
    is <- inputDao.findAllByTxsId(txsIds)
    os <- outputDao.findAllByTxsId(txsIds)
  } yield FullBlockInfo(h, links, txs, is, os)).transact[F](xa)

}
