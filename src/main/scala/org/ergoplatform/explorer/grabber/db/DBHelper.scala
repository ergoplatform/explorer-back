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
import org.ergoplatform._
import org.ergoplatform.explorer.grabber.protocol._
import org.postgresql.util.PGobject
import scapi.sigma.DLogProtocol.ProveDlog
import scorex.util.encode.Base16
import sigmastate.{SBoolean, SGroupElement, SType}
import sigmastate.Values.Value
import sigmastate.serialization.{OpCodes, ValueSerializer}

import scala.util.Try

class DBHelper(networkConfig: NetworkConfig) {

  private implicit val addressEncoder: ErgoAddressEncoder =
    ErgoAddressEncoder(if (networkConfig.testnet) Constants.testnetPrefix else Constants.testnetPrefix)

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
      val address: String = Base16.decode(o.proposition)
        .flatMap(scriptToAddress)
        .map(_.toString)
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

  def scriptToAddress(scriptBytes: Array[Byte]): Try[ErgoAddress] = Try {
    val p2shSample: Array[Byte] = new Pay2SHAddress(ErgoAddressEncoder.hash192(Array(0: Byte))).script.bytes
    val script: Value[SType] = ValueSerializer.deserialize(scriptBytes)
    scriptBytes.head match {
      case OpCodes.ProveDlogCode if scriptBytes.tail.head == (OpCodes.ConstantCode + SGroupElement.typeCode).toByte =>
        P2PKAddress(script.asInstanceOf[ProveDlog])
      case OpCodes.AndCode if scriptBytes.take(16) sameElements p2shSample.take(16) =>
        Pay2SHAddress(script.asInstanceOf[Value[SBoolean.type]])
      case _ => Pay2SAddress(script.asInstanceOf[Value[SBoolean.type]])
    }
  }
}
