package org.ergoplatform.explorer.generators

import org.ergoplatform.explorer.models.Header
import org.scalacheck.Arbitrary._
import org.scalacheck.{Arbitrary, Gen}
import scorex.crypto.encode.Base16

object HeadersGen {

  def headerGen(parentId: String, height: Int): Gen[Header] = for {
    id <- generateDigestString(32)
    pId = parentId
    version = 1: Short
    h = height
    adp <- generateDigestString(32)
    s <- generateDigestString(33)
    tr <- generateDigestString(32)
    nBits <- arbLong.arbitrary
    eHash <- generateDigestString(32)
    bz <- arbLong.arbitrary
    es <- Gen.listOfN(10, arbInt.arbitrary)
    ad <- Gen.oneOf(Gen.const(None), Gen.listOfN(32, arbByte.arbitrary).map(v => Some(v.toArray)))
  } yield Header(id, pId, version, h, adp, s, tr, System.currentTimeMillis(), nBits, eHash, bz, es, ad)

  val rootParentId = Base16.encode(Array.fill(32)(1: Byte))

  def generateHeaders(cnt: Int = 50): List[Header] = (0 until cnt).foldLeft(List.empty[Header]) { case (l, h) =>
    val pId = l.headOption.fold(rootParentId) { _.id }
    headerGen(pId, h).sample.get :: l
  }

}
