package org.ergoplatform.explorer.services

import cats._
import cats.data.NonEmptyList
import cats.effect._
import cats.implicits._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.explorer.db.dao._
import org.ergoplatform.explorer.db.models.Header
import org.ergoplatform.explorer.http.protocol.{
  BlockReferencesInfo,
  BlockSummaryInfo,
  FullBlockInfo,
  SearchBlockInfo
}
import org.ergoplatform.explorer.utils.{Paging, Sorting}

import scala.concurrent.ExecutionContext

trait BlockService[F[_]] {

  /**
    * @param id base58 representation of id byte array
    * @return fullBlockInfo
    */
  def getBlock(id: String): F[BlockSummaryInfo]

  /**
    * @param d base58 representation of id byte array
    * @return fullBlockInfo
    */
  def getBlockByD(d: String): F[BlockSummaryInfo]

  /**
    * Getting list of blocks
    *
    * @param p paging
    * @param s sorting
    * @return list of search block info
    */
  def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlockInfo]]

  def count(startTs: Long, endTs: Long): F[Long]

  /** Search blocks by the fragment of the header identifier
    */
  def searchById(query: String): F[List[SearchBlockInfo]]

}

class BlocksServiceIOImpl[F[_]](xa: Transactor[F], ec: ExecutionContext)(
  implicit F: Monad[F],
  A: Async[F]
) extends BlockService[F] {

  val headersDao = new HeadersDao
  val blockListDao = new BlockListDao
  val transactionsDao = new TransactionsDao
  val inputDao = new InputsDao
  val outputDao = new OutputsDao
  val adProofDao = new AdProofsDao
  val blockInfoDao = new BlockInfoDao
  val extensionDao = new BlockExtensionDao

  override def getBlock(id: String): F[BlockSummaryInfo] =
    for {
      _      <- Async.shift[F](ec)
      result <- getBlockResult(id)
    } yield result

  override def getBlockByD(d: String): F[BlockSummaryInfo] =
    for {
      _      <- Async.shift[F](ec)
      result <- getBlockByDResult(d)
    } yield result

  override def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): F[List[SearchBlockInfo]] =
    for {
      _ <- Async.shift[F](ec)
      rawBlocks <- blockListDao
        .list(p.offset, p.limit, s.sortBy, s.order.toString, start, end)
        .transact[F](xa)
      blocks = rawBlocks.map(SearchBlockInfo.fromRawSearchBlock)
    } yield blocks

  override def count(startTs: Long, endTs: Long): F[Long] =
    for {
      _   <- Async.shift[F](ec)
      cnt <- blockListDao.count(startTs, endTs).transact[F](xa)
    } yield cnt

  def searchById(substring: String): F[List[SearchBlockInfo]] =
    Async.shift[F](ec) *> blockListDao
      .searchById(substring)
      .map(_.map(SearchBlockInfo.fromRawSearchBlock))
      .transact[F](xa)

  private def getBlockResult(id: String): F[BlockSummaryInfo] =
    (for {
      h   <- headersDao.get(id)
      sum <- blockSummaryByHeader(h)
    } yield sum).transact[F](xa)

  private def getBlockByDResult(d: String): F[BlockSummaryInfo] =
    (for {
      h   <- headersDao.getByD(d)
      sum <- blockSummaryByHeader(h)
    } yield sum).transact[F](xa)

  private def blockSummaryByHeader(h: Header): ConnectionIO[BlockSummaryInfo] =
    for {
      nextIdOpt <- headersDao.findByParentId(h.id)
      references = BlockReferencesInfo(h.parentId, nextIdOpt.map(_.id))
      txs <- transactionsDao.findAllByBlockId(h.id)
      txsIds = txs.map(_.id)
      currentHeight <- headersDao.getLast(1).map(_.headOption.map(_.height).getOrElse(0L))
      confirmations <- if (txsIds.isEmpty) {
        List.empty[(String, Long)].pure[ConnectionIO]
      } else {
        transactionsDao
          .txsHeights(NonEmptyList.fromListUnsafe(txsIds))
          .map { list =>
            list.map(v => v._1 -> (currentHeight - v._2 + 1L))
          }
      }
      blockSize <- blockInfoDao.find(h.id).map(_.map(_.blockSize).getOrElse(0L))
      is        <- inputDao.findAllByTxsIdWithValue(txsIds)
      os        <- outputDao.findAllByTxsIdWithSpent(txsIds)
      ad        <- adProofDao.find(h.id)
      ext       <- extensionDao.getByHeaderId(h.id)
    } yield
      BlockSummaryInfo(FullBlockInfo(h, txs, confirmations, is, os, ext, ad, blockSize), references)

}
