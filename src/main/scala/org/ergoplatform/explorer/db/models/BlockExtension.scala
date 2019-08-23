package org.ergoplatform.explorer.db.models

import io.circe.{Decoder, Encoder, HCursor, Json}
import io.circe.syntax._

final case class BlockExtension(headerId: String, digest: String, fields: Json)

object BlockExtension {

  implicit val decoder: Decoder[BlockExtension] = { c: HCursor =>
    for {
      headerId <- c.downField("headerId").as[String]
      digest   <- c.downField("digest").as[String]
      fields   <- c.downField("fields").as[Json]
    } yield BlockExtension(headerId, digest, fields)
  }

  implicit val encoder: Encoder[BlockExtension] = { obj =>
    Json.obj(
      "headerId" -> obj.headerId.asJson,
      "digest"   -> obj.digest.asJson,
      "fields"   -> obj.fields
    )
  }

}
