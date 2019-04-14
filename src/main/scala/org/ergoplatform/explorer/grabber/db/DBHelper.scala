package org.ergoplatform.explorer.grabber.db

import cats.implicits._
import doobie._
import doobie.free.connection.ConnectionIO
import doobie.implicits._
import doobie.postgres.implicits._
import io.circe.Json
import io.circe.parser._
import org.ergoplatform._
import org.ergoplatform.explorer.Constants
import org.ergoplatform.explorer.config.ProtocolConfig
import org.ergoplatform.explorer.grabber.protocol._
import org.postgresql.util.PGobject
import scorex.util.encode.Base16
import sigmastate.serialization.ErgoTreeSerializer

class DBHelper(networkConfig: ProtocolConfig) {

  private val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(if (networkConfig.testnet) Constants.TestnetPrefix else Constants.MainnetPrefix)

  private val treeSerializer: ErgoTreeSerializer = new ErgoTreeSerializer

  implicit val MetaDifficulty: Meta[ApiDifficulty] = Meta[BigDecimal].xmap(
    x => ApiDifficulty(x.toBigInt()),
    x => BigDecimal.apply(x.value)
  )

  implicit val JsonMeta: Meta[Json] =
    Meta.other[PGobject]("json").xmap[Json](
      a => parse(a.getValue).leftMap[Json](e => throw e).merge,
      a => {
        val o = new PGobject
        o.setType("json")
        o.setValue(a.noSpaces)
        o
      }
    )

  def writeOne(fullBlock: ApiFullBlock): ConnectionIO[Int] = for {
    hInt <- NodeHeadersWriter.insert(fullBlock.header)
    txInt <- NodeTxWriter.insertMany(btToDb(fullBlock.transactions, fullBlock.header.timestamp))
    isInt <- NodeInputWriter.insertMany(btToInputs(fullBlock.transactions))
    osInt <- NodeOutputWriter.insertMany(btToOutputs(fullBlock.transactions, fullBlock.header.timestamp))
    adInt <- NodeAdProofsWriter.insertMany(nfbToAd(fullBlock))
  } yield hInt + txInt + isInt + osInt + adInt

  def btToDb(bt: ApiBlockTransactions, ts: Long): List[NodeTxWriter.ToInsert] = {
    val txs = bt.transactions
    val coinbaseId = txs.last.id
    val coinbaseTx = (coinbaseId, bt.headerId, true, ts, txs.last.size)
    val restTxs = txs.init.map { tx => (tx.id, bt.headerId, false, ts, tx.size) }
    coinbaseTx :: restTxs
  }

  def nodeInputsToDb(txId: String, inputs: List[ApiInput]): List[NodeInputWriter.ToInsert] = inputs
    .map { i => (i.boxId, txId, i.spendingProof.proofBytes, i.spendingProof.extension) }

  def nodeOutputsToDb(txId: String, outputs: List[ApiOutput], ts: Long): List[NodeOutputWriter.ToInsert] = outputs
    .zipWithIndex
    .map { case (o, index) =>
      val address: String = Base16.decode(o.ergoTree)
        .flatMap { bytes => addressEncoder.fromProposition(treeSerializer.deserializeErgoTree(bytes).proposition) }
        .map { _.toString }
        .getOrElse("unable to derive address from given ErgoTree")
      (o.boxId, txId, o.value, o.creationHeight, index, o.ergoTree, address, o.assets, o.additionalRegisters, ts)
    }

  def btToInputs(bt: ApiBlockTransactions): List[NodeInputWriter.ToInsert] = bt.transactions.flatMap { tx =>
    nodeInputsToDb(tx.id, tx.inputs)
  }

  def btToOutputs(bt: ApiBlockTransactions, ts: Long): List[NodeOutputWriter.ToInsert] = bt.transactions.flatMap { tx =>
    nodeOutputsToDb(tx.id, tx.outputs, ts)
  }

  def nfbToAd(nfb: ApiFullBlock): List[NodeAdProofsWriter.ToInsert] = nfb.adProofs.toList.map { ad =>
    (ad.headerId, ad.proofBytes, ad.digest)
  }

  def readCurrentHeight: ConnectionIO[Long] =
    fr"SELECT COALESCE(height, CAST(0 as BIGINT)) FROM blocks_info ORDER BY height DESC LIMIT 1"
      .query[Long].option.map { _.getOrElse(Constants.PreGenesisHeight) }
}
