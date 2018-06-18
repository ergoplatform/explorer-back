package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.http.protocol.{BlockReferencesInfo, BlockSummaryInfo, FullBlockInfo, SearchBlock}
import org.ergoplatform.explorer.utils.{Paging, Sorting}

import scala.concurrent.ExecutionContext

trait BlockService[F[_]] {

  /**
    * @param id base58 representation of id byte array
    * @return fullBlockInfo
    */
  def getBlock(id: String): F[BlockSummaryInfo]

  /**
    * Getting list of blocks
    *
    * @param p paging
    * @param s sorting
    * @return list of search block info
    */
  def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlock]]

  def count(startTs: Long, endTs: Long): F[Long]
}

class BlocksServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                               (implicit F: Monad[F], A: Async[F]) extends BlockService[F] {

  val headersDao = new HeadersDao
  val interlinksDao = new InterlinksDao
  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao

  override def getBlock(id: String): F[BlockSummaryInfo] = for {
    _ <- Async.shift[F](ec)
    result <- getBlockResult(id)
  } yield result

  private def getBlockResult(id: String): F[BlockSummaryInfo] = (for {
    h <- headersDao.get(id)
    nextIdOpt <- headersDao.findByParentId(h.id)
    references = BlockReferencesInfo(h.parentId, nextIdOpt.map(_.id))
    links <- interlinksDao.findAllByBlockId(h.id)
    txs <- transactionsDao.findAllByBlockId(h.id)
    txsIds = txs.map(_.id)
    is <- inputDao.findAllByTxsId(txsIds)
    os <- outputDao.findAllByTxsId(txsIds)
  } yield BlockSummaryInfo(FullBlockInfo(h, links, txs, is, os), references)).transact[F](xa)

  override def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlock]] = for {
    _ <- Async.shift[F](ec)
    result <- getBlocksResult(p, s, start, end)
  } yield result

  private def getBlocksResult(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlock]] = (for {
    h <- headersDao.list(p.offset, p.limit, s.sortBy, s.order.toString, start, end)
    hIds = h.map(_.id)
    txsCnt <- transactionsDao.countTxsNumbersByBlocksIds(hIds)
    result = h
      .map { h => h -> txsCnt.find(_._1 == h.id).map(_._2).getOrElse(0L) }
      .map{ case (info, count) => SearchBlock.fromHeader(info, count)}
  } yield result).transact[F](xa)

  override def count(startTs: Long, endTs: Long): F[Long] = for {
    _ <- Async.shift[F](ec)
    cnt <- headersDao.count(startTs, endTs).transact[F](xa)
  } yield cnt

}
