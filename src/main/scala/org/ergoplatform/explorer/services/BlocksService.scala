package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.http.protocol.{FullBlockInfo, SearchBlock}
import org.ergoplatform.explorer.utils.{Paging, Sorting}
import scorex.crypto.encode.{Base16, Base58}

import scala.concurrent.ExecutionContext


trait BlockService[F[_]] {

  /**
    * @param id base58 representation of id byte array
    * @return fullBlockInfo
    */
  def getBlock(id: String): F[FullBlockInfo]

  /**
    * Getting list of blocks
    *
    * @param p paging
    * @param s sorting
    * @return list of search block info
    */
  def getBlocks(p: Paging, s: Sorting): F[List[SearchBlock]]
}

class BlocksServiceImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                             (implicit F: Monad[F], A: Async[F]) extends BlockService[F] {

  val headersDao = new HeadersDao
  val interlinksDao = new InterlinksDao
  val transactionsDao = new TransactionsDao
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
    txs <- transactionsDao.findAllByBLockId(h.id)
    txsIds = txs.map(_.id)
    is <- inputDao.findAllByTxsId(txsIds)
    os <- outputDao.findAllByTxsId(txsIds)
  } yield FullBlockInfo(h, links, txs, is, os)).transact[F](xa)

  override def getBlocks(p: Paging, s: Sorting): F[List[SearchBlock]] = for {
    _ <- Async.shift[F](ec)
    result <- getBlocksResult(p, s)
  } yield result

  private def getBlocksResult(p: Paging, s: Sorting): F[List[SearchBlock]] = (for {
    h <- headersDao.list(p.offset, p.limit, s.sortBy, s.order.toString)
    hIds = h.map(_.id)
    txsCnt <- transactionsDao.countTxsNumbersByBlocksIds(hIds)
    result = h
      .map { h => h -> txsCnt.find(_._1 == h.id).map(_._2).getOrElse(0) }
      .map{ case (info, count) => SearchBlock.fromHeader(info, count)}
  } yield result).transact[F](xa)

}
