package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

final case class ForksSummary(qty: Int, forks: List[ForkInfo])

object ForksSummary {

  implicit val encoder: Encoder[ForksSummary] = { obj =>
    Json.obj(
      "quantity" -> obj.qty.asJson,
      "forks"    -> obj.forks.asJson
    )
  }

}
