package org.ergoplatform.explorer.grabber.db

import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.free.connection.ConnectionIO
import io.circe.Json
import io.circe.parser._
import org.ergoplatform.explorer.config.NetworkConfig
import org.ergoplatform.explorer.grabber.Constants
import org.ergoplatform.{ErgoAddressEncoder, Pay2SAddress}
import org.ergoplatform.explorer.grabber.protocol._
import org.postgresql.util.PGobject
import scorex.util.encode.Base16
import sigmastate.SBoolean
import sigmastate.Values.Value
import sigmastate.serialization.ValueSerializer

class DBHelper(networkConfig: NetworkConfig) {

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

  def writeOne(nfb: ApiFullBlock): ConnectionIO[Int] = for {
    hInt <- NodeHeadersWriter.insert(nfb.header)
    txInt <- NodeTxWriter.insertMany(btToDb(nfb.bt, nfb.header.timestamp))
    isInt <- NodeInputWriter.insertMany(btToInputs(nfb.bt))
    osInt <- NodeOutputWriter.insertMany(btToOutputs(nfb.bt, nfb.header.timestamp))
    adInt <- NodeAdProofsWriter.insertMany(nfbToAd(nfb))
  } yield hInt + txInt + isInt + osInt + adInt

  def btToDb(bt: ApiBlockTransactions, ts: Long): List[NodeTxWriter.ToInsert] = {
    val txs = bt.transactions
    val coinbaseId = txs.last.id
    val coinbaseTx = (coinbaseId, bt.headerId, true, ts, txs.last.size)

    val restTxs = txs.init.map { tx => (tx.id, bt.headerId, false, ts, tx.size) }
    coinbaseTx :: restTxs
  }

  def nodeInputsToDb(txId: String, list: List[ApiInput]): List[NodeInputWriter.ToInsert] = list
    .map { i => (i.boxId, txId, i.spendingProof.proofBytes, i.spendingProof.extension) }

  def nodeOutputsToDb(txId: String, list: List[ApiOutput], ts: Long): List[NodeOutputWriter.ToInsert] = list
    .zipWithIndex
    .map { case (o, index) =>
      val networkPrefix: Byte = if (networkConfig.testnet) Constants.testnetPrefix else Constants.testnetPrefix
      val encoder: ErgoAddressEncoder = ErgoAddressEncoder(networkPrefix)
      val address: String = Base16.decode(o.proposition)
        .map(r => new Pay2SAddress(ValueSerializer.deserialize(r).asInstanceOf[Value[SBoolean.type]], r)(encoder).toString)
        .getOrElse(o.proposition)
      (o.boxId, txId, o.value, index, o.proposition, address, o.additionalRegisters, ts)
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
    fr"SELECT COALESCE(height, CAST(0 as BIGINT)) FROM node_headers ORDER BY height DESC LIMIT 1"
      .query[Long].option.map { _.getOrElse(-1L) }
}
