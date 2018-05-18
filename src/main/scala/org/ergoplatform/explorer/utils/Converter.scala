package org.ergoplatform.explorer.utils

import scorex.crypto.encode.{Base16, Base58}

object Converter {

  def from16to58(s: String): String = Base58.encode(Base16.decode(s).get)

  def from58to16(s: String): String = Base16.encode(Base58.decode(s).get)
}
