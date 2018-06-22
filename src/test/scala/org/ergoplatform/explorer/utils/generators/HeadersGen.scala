package org.ergoplatform.explorer.utils.generators

import org.ergoplatform.explorer.db.models.Header
import org.scalacheck.Arbitrary._
import org.scalacheck.Gen
import scorex.crypto.encode.Base16

object HeadersGen {

  val rootId = Base16.encode(Array.fill(32)(1: Byte))

  val initBlock = headerGen(rootId, -1).sample.get.copy(id = rootId, parentId = Base16.encode(Array.fill(32)(0: Byte)))

  def headerGen(parentId: String, height: Long): Gen[Header] = for {
    id <- generateDigestStringBase16(32)
    pId = parentId
    version = 1: Short
    h = height
    adp <- generateDigestStringBase16(32)
    s <- generateDigestStringBase16(33)
    tr <- generateDigestStringBase16(32)
    nBits <- arbLong.arbitrary
    eHash <- generateDigestStringBase16(32)
    bz <- arbLong.arbitrary
    es <- generateDigestStringBase16(32)
    ad <- Gen.oneOf(Gen.const(None), Gen.listOfN(32, arbByte.arbitrary).map(v => Some(v.toArray)))
    reward <- Gen.choose(1000000000L, 10000000000L)
    fee <- Gen.choose(1L, 1000000L)
  } yield Header(id, pId, version, h, nBits, 0L, System.currentTimeMillis(), s, adp, tr,  eHash, es, List.empty[String], 0L)

  def generateHeaders(cnt: Int = 50): List[Header] = (0 until cnt).foldLeft(List(initBlock)) { case (l, h) =>
    val pId = l.headOption.fold(rootId) { _.id }
    headerGen(pId, h).sample.get :: l
  }

}
