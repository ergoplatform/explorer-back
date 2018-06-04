package org.ergoplatform.explorer.utils

import com.typesafe.scalalogging.Logger
import scorex.crypto.encode.{Base16, Base58}

import scala.util.{Success, Failure}

object Converter {

  val logger = Logger("16_58_converter")

  def from16to58(s: String): String = Base58.encode{
    Base16.decode(s) match {
      case Success(v) =>
        v
      case Failure(t) =>
        logger.error(s"Cannot convert value $s from 16 to 58")
        throw t
    }
  }

  def from58to16(s: String): String = Base16.encode{
    Base58.decode(s)  match {
      case Success(v) =>
        v
      case Failure(t) =>
        logger.error(s"Cannot convert value $s from 58 to 16")
        throw t
    }
  }
}
