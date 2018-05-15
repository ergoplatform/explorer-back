package org.ergoplatform.explorer.generators

import org.ergoplatform.explorer.models.{Header, Transaction}
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import scorex.crypto.encode.Base16

object TransactionsGenerator {

  def generate(blockId: String, isCoinbase: Boolean = false): Gen[Transaction] =
    Gen.listOfN(32, arbByte.arbitrary).map { l => Transaction(Base16.encode(l.toArray), blockId, isCoinbase) }

  def generateForBlock(blockId: String): Gen[List[Transaction]] = for {
    cb <- generate(blockId, true)
    number <- Gen.chooseNum(1, 10)
    other <- Gen.listOfN(number, generate(blockId, false))
  } yield cb +: other

  def generateForBlocks(list: List[Header]): List[Transaction] = list.flatMap { h => generateForBlock(h.id).sample.get }


}
