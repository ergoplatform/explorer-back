package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}

case class ItemsResponse[A](items: List[A], total: Long)

object ItemsResponse {

  implicit def ecoderMinerInfo[A](implicit e: Encoder[A]): Encoder[ItemsResponse[A]] =
    (i: ItemsResponse[A]) => Json.obj(
      ("items", i.items.asJson),
      ("total", Json.fromLong(i.total))
  )
}
