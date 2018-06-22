package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.models.Header
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

  def searchById(query: String): F[List[SearchBlock]]

}

class BlocksServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)
                               (implicit F: Monad[F], A: Async[F]) extends BlockService[F] {

  val headersDao = new HeadersDao
  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao
  val adProofDao = new AdProofsDao
  val blockInfoDao = new BlockInfoDao

  override def getBlock(id: String): F[BlockSummaryInfo] = for {
    _ <- Async.shift[F](ec)
    result <- getBlockResult(id)
  } yield result

  private def getBlockResult(id: String): F[BlockSummaryInfo] = (for {
    h <- headersDao.get(id)
    nextIdOpt <- headersDao.findByParentId(h.id)
    references = BlockReferencesInfo(h.parentId, nextIdOpt.map(_.id))
    txs <- transactionsDao.findAllByBlockId(h.id)
    txsIds = txs.map(_.id)
    is <- inputDao.findAllByTxsId(txsIds)
    os <- outputDao.findAllByTxsId(txsIds)
    ad <- adProofDao.find(h.id)
  } yield BlockSummaryInfo(FullBlockInfo(h, txs, is, os, ad), references)).transact[F](xa)

  override def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlock]] = for {
    _ <- Async.shift[F](ec)
    result <- getBlocksResult(p, s, start, end)
  } yield result

  private def getBlocksResult(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlock]] = {
    enrichSearchBlocks(headersDao.list(p.offset, p.limit, s.sortBy, s.order.toString, start, end)).transact[F](xa)
  }

  override def count(startTs: Long, endTs: Long): F[Long] = for {
    _ <- Async.shift[F](ec)
    cnt <- headersDao.count(startTs, endTs).transact[F](xa)
  } yield cnt

  /** Search blocks by the fragment of the header identifier */
  def searchById(substring: String): F[List[SearchBlock]] = {
    enrichSearchBlocks(headersDao.searchById(substring)).transact[F](xa)
  }

  private def enrichSearchBlocks(headersIO: ConnectionIO[List[Header]]): ConnectionIO[List[SearchBlock]] = {
    for {
      headers <- headersIO
      transactionCounts <- transactionsDao.countTxsNumbersByBlocksIds(headers.map(_.id))
    } yield constructSearchBlocks(headers, transactionCounts)
  }

  private def constructSearchBlocks(headers: List[Header],
                                    transactionCounts: List[(String, Long)]): List[SearchBlock] = {
    headers.map { header =>
      val transactionCount = transactionCounts
        .find { case (id, _) => id == header.id }
        .map { case (_, count) => count }
        .getOrElse(0L)
      SearchBlock.fromHeader(header, transactionCount)
    }
  }

}
