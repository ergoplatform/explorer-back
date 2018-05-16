package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.dao._
import org.ergoplatform.explorer.models.Block

import scala.concurrent.ExecutionContext


trait BlockService[F[_]] {

  def getBlock(id: String): F[Block]
}

class BlocksServiceImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                             (implicit F: Monad[F], A: Async[F]) extends BlockService[F] {

  val headersDao = new HeadersDao
  val interlinksDao = new InterlinksDao
  val transactionsDap = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao


  override def getBlock(id: String): F[Block] = for {
    _ <- Async.shift[F](ec)
    result <- getBlockResult(id)
  } yield result

  private def getBlockResult(id: String): F[Block] = (for {
    h <- headersDao.get(id)
    links <- interlinksDao.findAllByBLockId(h.id)
    txs <- transactionsDap.findAllByBLockId(h.id)
    txsIds = txs.map(_.id)
    is <- inputDao.findAllByTxsId(txsIds)
    os <- outputDao.findAllByTxsId(txsIds)
  } yield Block(h, links, txs, is, os)).transact[F](xa)

}
