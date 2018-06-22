package org.ergoplatform.explorer.grabber.db


import cats.data._
import cats.effect.IO
import cats.implicits._
import doobie._
import doobie.implicits._
import doobie.postgres.implicits._
import doobie.free.connection.ConnectionIO
import io.circe.Json
import io.circe.parser._
import org.ergoplatform.explorer.grabber.protocol._
import org.postgresql.util.PGobject


object DBHelper {

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
    osInt <- NodeOutputWriter.insertMany(btToOutputs(nfb.bt))
    adInt <- NodeAdProofsWriter.insertMany(nfbToAd(nfb))
  } yield hInt + txInt + isInt + osInt + adInt

  def btToDb(bt: ApiBlockTransactions, ts: Long): List[NodeTxWriter.ToInsert] = {
    val txs = bt.transactions
    val coinbaseId = txs.head.id
    val coinbaseTx = (coinbaseId, bt.headerId, true, ts)
    val restTxs = txs.tail.map { tx => (tx.id, bt.headerId, false, ts) }
    coinbaseTx :: restTxs
  }

  def nodeInputsToDb(txId: String, list: List[ApiInput]): List[NodeInputWriter.ToInsert] = list
    .map { i => (i.boxId, txId, i.spendingProof.proofBytes, i.spendingProof.extension) }

  def nodeOutputsToDb(txId: String, list: List[ApiOutput]): List[NodeOutputWriter.ToInsert] = list
    .zipWithIndex.map { case (o, index) =>
    (o.boxId, txId, o.value, index, o.proposition, o.proposition, o.additionalRegisters)
  }

  def btToInputs(bt: ApiBlockTransactions): List[NodeInputWriter.ToInsert] = bt.transactions.flatMap { tx =>
    nodeInputsToDb(tx.id, tx.inputs)
  }

  def btToOutputs(bt: ApiBlockTransactions): List[NodeOutputWriter.ToInsert] = bt.transactions.flatMap { tx =>
    nodeOutputsToDb(tx.id, tx.outputs)
  }

  def nfbToAd(nfb: ApiFullBlock): List[NodeAdProofsWriter.ToInsert] = nfb.adProofs.toList.map { ad =>
    (ad.headerId, ad.proofBytes, ad.digest)
  }

  def readCurrentHeight: ConnectionIO[Long] =
    fr"SELECT COALESCE(height, CAST(0 as BIGINT)) FROM node_headers ORDER BY height DESC LIMIT 1"
      .query[Long].option.map { _.getOrElse(-1L) }
}
