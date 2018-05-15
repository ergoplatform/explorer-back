package org.ergoplatform.explorer.generators

import org.ergoplatform.explorer.models.{Header, Interlink}
import org.scalacheck.Gen

object InterlinksGenerator {

  def generate(allIds: List[String], blockId: String): Gen[List[(String, String)]] = for {
    size <- Gen.chooseNum(0, 10)
    links <- Gen.oneOf(Gen.const(List.empty),
      Gen.listOfN(size, Gen.oneOf(allIds).map(v => v -> blockId)).map(_.distinct))
  } yield links


  def generateInterlinks(headers: List[Header]): List[(String, String)] = {
    val allIds = headers.map(_.id)
    headers.flatMap { h => generate(allIds.filterNot(_ == h.id), h.id).sample.get }
  }

}
