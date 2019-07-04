package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

final case class ForkInfo(length: Int,
                          branchPointHeightOpt: Option[Long],
                          members: List[(Long, String)]) {

  val orphaned: Boolean = branchPointHeightOpt.isEmpty

}

object ForkInfo {

  implicit val encoder: Encoder[ForkInfo] = { obj =>
    Json.obj(
      "length" -> obj.length.asJson,
      "branchPointHeight" -> obj.branchPointHeightOpt.asJson,
      "orphaned" -> obj.orphaned.asJson,
      "members" -> obj.members.asJson
    )
  }

}
