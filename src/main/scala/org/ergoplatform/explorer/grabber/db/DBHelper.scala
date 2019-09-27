package org.ergoplatform.explorer.grabber.db

import doobie.free.connection.ConnectionIO
import doobie.implicits._
import org.ergoplatform._
import org.ergoplatform.explorer.config.ProtocolConfig
import org.ergoplatform.explorer.db.mappings.JsonMeta
import org.ergoplatform.explorer.grabber.protocol._
import org.ergoplatform.explorer.{Constants, Utils}

class DBHelper(networkConfig: ProtocolConfig) extends JsonMeta {

  implicit val addressEncoder: ErgoAddressEncoder = networkConfig.addressEncoder

  def writeOne(fullBlock: ApiFullBlock): ConnectionIO[Unit] =
    for {
      _ <- HeaderWriter.insert(fullBlock.header)
      _ <- TransactionWriter.insertMany(
        btToDb(fullBlock.transactions, fullBlock.header.timestamp)
      )
      _ <- InputWriter.insertMany(btToInputs(fullBlock.transactions))
      _ <- OutputWriter.insertMany(
        btToOutputs(fullBlock.transactions, fullBlock.header.timestamp)
      )
      _ <- AssetsWriter.insertMany(btToAssets(fullBlock.transactions))
      _ <- AdProofsWriter.insertMany(nfbToAd(fullBlock))
      _ <- BlockExtensionWriter.insert(fullBlock.extension)
    } yield ()

  def btToDb(bt: ApiBlockTransactions, ts: Long): List[TransactionWriter.ToInsert] = {
    val txs = bt.transactions
    val coinbaseId = txs.last.id
    val coinbaseTx = (coinbaseId, bt.headerId, true, ts, txs.last.size)
    val restTxs = txs.init.map { tx =>
      (tx.id, bt.headerId, false, ts, tx.size)
    }
    coinbaseTx :: restTxs
  }

  def nodeInputsToDb(txId: String, inputs: List[ApiInput]): List[InputWriter.ToInsert] =
    inputs
      .map { i =>
        (i.boxId, txId, i.spendingProof.proofBytes, i.spendingProof.extension)
      }

  def nodeOutputsToDb(
    txId: String,
    outputs: List[ApiOutput],
    ts: Long
  ): List[OutputWriter.ToInsert] =
    outputs.zipWithIndex
      .map {
        case (o, index) =>
          val address: String = Utils
            .ergoTreeToAddress(o.ergoTree)
            .map { _.toString }
            .getOrElse("unable to derive address from given ErgoTree")
          (
            o.boxId,
            txId,
            o.value,
            o.creationHeight,
            index,
            o.ergoTree,
            address,
            o.additionalRegisters,
            ts
          )
      }

  def btToInputs(bt: ApiBlockTransactions): List[InputWriter.ToInsert] = bt.transactions.flatMap {
    tx =>
      nodeInputsToDb(tx.id, tx.inputs)
  }

  def btToOutputs(bt: ApiBlockTransactions, ts: Long): List[OutputWriter.ToInsert] =
    bt.transactions.flatMap { tx =>
      nodeOutputsToDb(tx.id, tx.outputs, ts)
    }

  def btToAssets(bt: ApiBlockTransactions): List[AssetsWriter.ToInsert] =
    for {
      tx <- bt.transactions
      out <- tx.outputs
      assets <- out.assets
    } yield (assets.tokenId, out.boxId, assets.amount)

  def nfbToAd(nfb: ApiFullBlock): List[AdProofsWriter.ToInsert] = nfb.adProofs.toList.map { ad =>
    (ad.headerId, ad.proofBytes, ad.digest)
  }

  def readCurrentHeight: ConnectionIO[Long] =
    fr"SELECT COALESCE(height, CAST(0 as BIGINT)) FROM blocks_info ORDER BY height DESC LIMIT 1"
      .query[Long]
      .option
      .map { _.getOrElse(Constants.PreGenesisHeight) }
}
