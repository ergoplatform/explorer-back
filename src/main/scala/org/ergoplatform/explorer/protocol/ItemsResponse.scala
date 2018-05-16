package org.ergoplatform.explorer.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

case class ItemsResponse[A](items: List[A], total: Int)

object ItemsResponse {

  implicit def ecoderMinerInfo[A](implicit e: Encoder[A]): Encoder[ItemsResponse[A]] =
    (i: ItemsResponse[A]) => Json.obj(
      ("items", i.items.asJson),
      ("total", Json.fromInt(i.total))
  )
}
