package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor}

case class ApiPowSolutions(pk: String, w: String, n: String, d: BigInt) {

  override def toString: String = s"(pk: $pk, w: $w, n: $n, d: $d)"
}

object ApiPowSolutions {

  implicit val jsonDecoder: Decoder[ApiPowSolutions] = { c: HCursor =>
    for {
      pk <- c.downField("pk").as[String]
      w <- c.downField("w").as[String]
      n <- c.downField("n").as[String]
      d <- c.downField("d").as[BigInt]
    } yield ApiPowSolutions(pk, w, n, d: BigInt)
  }

}
