package org.ergoplatform.explorer.services

import cats._
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import doobie.implicits._
import doobie.util.transactor.Transactor
import org.ergoplatform.ErgoAddressEncoder
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ProtocolConfig
import org.ergoplatform.explorer.db.dao.{AddressDao, AssetsDao, OutputsDao}
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.db.models.Asset
import org.ergoplatform.explorer.db.models.composite.ExtendedOutput
import org.ergoplatform.explorer.grabber.protocol.ApiAsset
import org.ergoplatform.explorer.http.protocol.AddressInfo
import org.ergoplatform.explorer.persistence.TransactionsPool
import org.ergoplatform.explorer.utils.Paging
import scorex.util.encode.Base16
import sigmastate.serialization.ErgoTreeSerializer

import scala.concurrent.ExecutionContext

trait AddressesService[F[_]] {

  def getAddressInfo(addressId: String): F[AddressInfo]

  def searchById(query: String): F[List[String]]

  def holdersAddresses(assetId: String, p: Paging): F[List[String]]

}

final class AddressesServiceImpl[F[_]](
  xa: Transactor[F],
  txPoolRef: Ref[F, TransactionsPool],
  ec: ExecutionContext,
  cfg: ProtocolConfig
)(implicit F: Monad[F], A: Async[F])
  extends AddressesService[F]
    with JsonMeta {

  val outputsDao = new OutputsDao
  val addressDao = new AddressDao
  val assetsDao = new AssetsDao

  private val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(if (cfg.testnet) Constants.TestnetPrefix else Constants.MainnetPrefix)

  private val treeSerializer: ErgoTreeSerializer = new ErgoTreeSerializer

  override def holdersAddresses(
    assetId: String,
    p: Paging
  ): F[List[String]] =
    for {
      _         <- Async.shift[F](ec)
      addresses <- assetsDao.holderAddresses(assetId, p.offset, p.limit).transact(xa)
    } yield addresses

  override def getAddressInfo(addressId: String): F[AddressInfo] =
    for {
      _    <- Async.shift[F](ec)
      info <- getAddressInfoResult(addressId)
    } yield info

  private def ergoTreeToAddress(ergoTree: String) =
    Base16
      .decode(ergoTree)
      .flatMap { bytes =>
        addressEncoder.fromProposition(treeSerializer.deserializeErgoTree(bytes).proposition)
      }
      .map { _.toString }
      .getOrElse("unable to derive address from given ErgoTree")

  private def getOutputsWithAssets(
    address: String
  ): F[List[(ExtendedOutput, List[Asset])]] = {
    val txn = for {
      outputs <- outputsDao.findAllByAddress(address)
      outputsWithAssets <- outputs
        .map(out => assetsDao.getByBoxId(out.output.boxId).map(out -> _))
        .sequence
    } yield outputsWithAssets
    txn.transact(xa)
  }

  private def getAddressInfoResult(address: String): F[AddressInfo] =
    (getOutputsWithAssets(address), txPoolRef.get)
      .mapN { (outputs, txPool) =>
        val offChainTxs = txPool.getAll
        val offChainInputs = offChainTxs.flatMap(_.inputs.map(_.boxId))
        val (spentOnChainBoxes, unspentOnChainBoxes) = outputs
          .filter(_._1.mainChain)
          .partition(_._1.spentByOpt.isDefined)
        val spentOffChainBoxes = unspentOnChainBoxes
          .filter(o => offChainInputs.contains(o._1.output.boxId))
        val unspentOffChainBoxes = offChainTxs
          .flatMap(_.outputs)
          .filter(x => ergoTreeToAddress(x.ergoTree) == address)
        val confirmedTxsQty = outputs
          .map(_._1.output.txId)
          .distinct
          .size
        val spentOnChainBalance = spentOnChainBoxes
          .map(_._1.output.value)
          .sum
        val spentOffChainBalance = spentOffChainBoxes.map(_._1.output.value).sum
        val onChainBalance = unspentOnChainBoxes
          .map(_._1.output.value)
          .sum
        val offChainBalance = unspentOffChainBoxes.map(_.value).sum - spentOffChainBalance
        val totalBalance = onChainBalance + offChainBalance
        val onChainTokensBalance = unspentOnChainBoxes
          .flatMap(_._2)
          .foldLeft(Map.empty[String, Long]) {
            case (acc, Asset(assetId, _, assetAmt)) =>
              acc.updated(assetId, acc.getOrElse(assetId, 0L) + assetAmt)
          }
        val offChainTokensReceived = unspentOffChainBoxes
          .flatMap(_.assets)
          .foldLeft(Map.empty[String, Long]) {
            case (acc, ApiAsset(assetId, assetAmt)) =>
              acc.updated(assetId, acc.getOrElse(assetId, 0L) + assetAmt)
          }
        val offChainTokensSpent = spentOffChainBoxes
          .flatMap(_._2)
          .foldLeft(Map.empty[String, Long]) {
            case (acc, Asset(assetId, _, assetAmt)) =>
              acc.updated(assetId, acc.getOrElse(assetId, 0L) + assetAmt)
          }
        val existingTokens = onChainTokensBalance.keys.toSeq
        // new tokens this address received for the first time
        val newOffChainTokensBalance =
          offChainTokensReceived.filterNot(x => existingTokens.contains(x._1))
        val totalTokensBalance = newOffChainTokensBalance ++ onChainTokensBalance
          .foldLeft(Map.empty[String, Long]) {
            case (acc, (assetId, onChainAmt)) =>
              val offChainSpent = offChainTokensSpent.getOrElse(assetId, 0L)
              val offChainReceived = offChainTokensReceived.getOrElse(assetId, 0L)
              acc.updated(assetId, onChainAmt + offChainReceived - offChainSpent)
          }
        val totalReceived = spentOnChainBalance + onChainBalance
        val onChainAssets = onChainTokensBalance.map(x => ApiAsset(x._1, x._2)).toList
        val totalAssets = totalTokensBalance.map(x => ApiAsset(x._1, x._2)).toList
        AddressInfo(
          address,
          confirmedTxsQty,
          totalReceived,
          onChainBalance,
          totalBalance,
          onChainAssets,
          totalAssets
        )
      }

  def searchById(substring: String): F[List[String]] = {
    outputsDao.searchByAddressId(substring).transact(xa)
  }

}
