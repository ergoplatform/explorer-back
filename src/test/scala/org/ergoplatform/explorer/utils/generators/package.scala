package org.ergoplatform.explorer.utils

import org.scalacheck.Arbitrary.arbByte
import org.scalacheck.Gen
import scorex.crypto.encode.Base16

package object generators {

  def generateDigestStringBase16(length: Int): Gen[String] = Gen.listOfN(length, arbByte.arbitrary).map { l =>
    Base16.encode(l.toArray)
  }

}
