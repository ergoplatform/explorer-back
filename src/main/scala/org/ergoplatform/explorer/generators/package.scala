package org.ergoplatform.explorer

import org.scalacheck.Gen
import org.scalacheck.Arbitrary._
import scorex.crypto.encode.Base16

package object generators {

  def generateDigestString(length: Int): Gen[String] = Gen.listOfN(length, arbByte.arbitrary).map {l =>
    Base16.encode(l.toArray)
  }

}
