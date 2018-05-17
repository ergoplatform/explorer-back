package org.ergoplatform.explorer.utils.generators

import org.ergoplatform.explorer.db.models.{Header, Input, Output, Transaction}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import scorex.crypto.encode.Base16

import scala.collection.mutable.ArrayBuffer

object TransactionsGenerator {

  type TxData = (List[Transaction], List[Output], List[Input])

  def initTx: Gen[Transaction] = for {
    id <- generateDigestStringBase16(32)
  } yield Transaction(id, HeadersGen.rootId, false)


  def initOutputs(txId: String): Gen[List[Output]] = for {
    number <- Gen.chooseNum(1, 10)
    values <- Gen.listOfN(number, Gen.chooseNum(10000L, 100000L))
    ids <- Gen.listOfN(number, generateDigestStringBase16(32))
    outputs = ids.zip(values).map { case (id, v) =>
      Output(id, txId, v, false, "")
    }
  } yield outputs



  def generate(blockId: String, isCoinbase: Boolean = false): Gen[Transaction] =
    Gen.listOfN(32, arbByte.arbitrary).map { l => Transaction(Base16.encode(l.toArray), blockId, isCoinbase) }

  def generateForBlock(blockId: String): Gen[List[Transaction]] = for {
    cb <- generate(blockId, true)
    number <- Gen.chooseNum(1, 10)
    other <- Gen.listOfN(number, generate(blockId, false))
  } yield cb +: other

  def generateSomeData(blocks: List[Header]): (List[Transaction], List[Output], List[Input]) = {
    val txs = ArrayBuffer.empty[Transaction]
    var osNotSpent = List.empty[Output]
    val osSpent = ArrayBuffer.empty[Output]
    val is = ArrayBuffer.empty[Input]


    blocks.sortBy(_.height).foreach { h =>

      if (h.height == -1) {
        val txId = generateDigestStringBase16(32).sample.get
        val os = initOutputs(txId).sample.get
        val tx = Transaction(txId, h.id, false)
        txs.append(tx)
        osNotSpent = os
      } else {
        val data = osNotSpent.map { o =>
          val txId = generateDigestStringBase16(32).sample.get
          val id = generateDigestStringBase16(32).sample.get
          val i = Input(id, txId, o.id, "")

          val oId1 = generateDigestStringBase16(32).sample.get
          val v1 = o.value / 2
          val o1 = Output(oId1, txId, v1, false, "")

          val oId2 = generateDigestStringBase16(32).sample.get
          val v2 = o.value - v1
          val o2 = Output(oId2, txId, v2, false, "")

          (i , List(o1, o2))
        }

        val inputs = data.map(_._1)
        val outputs = data.flatMap(_._2)
        val newTxs = inputs.map { i =>
          Transaction(i.txId, h.id, false)
        }

        txs ++= newTxs
        osSpent ++= osNotSpent.map(_.copy(spent = true))
        osNotSpent = outputs
        is ++= inputs
      }
    }
    (txs.toList, (osSpent ++= osNotSpent).toList, is.toList)
  }
}
