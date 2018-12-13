package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, Encoder, HCursor}
import io.circe.syntax._

case class ApiPowSolutions(pk: String, w: String, n: String, d: String)

object ApiPowSolutions {

  implicit val jsonEncoder: Encoder[ApiPowSolutions] = { sol: ApiPowSolutions =>
    Map(
      "pk" -> sol.pk.asJson,
      "w" -> sol.w.asJson,
      "n" -> sol.n.asJson,
      "d" -> BigInt(sol.d).asJson
    ).asJson
  }

  implicit val jsonDecoder: Decoder[ApiPowSolutions] = { c: HCursor =>
    for {
      pk <- c.downField("pk").as[String]
      w <- c.downField("w").as[String]
      n <- c.downField("n").as[String]
      d <- c.downField("d").as[BigInt]
    } yield ApiPowSolutions(pk, w, n, d.toString())
  }

}
