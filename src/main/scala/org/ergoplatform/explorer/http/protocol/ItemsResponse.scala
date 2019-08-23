package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}

final case class ItemsResponse[A](items: List[A], total: Long)

object ItemsResponse {

  implicit def encoder[A](implicit e: Encoder[A]): Encoder[ItemsResponse[A]] = { i =>
    Json.obj(
      ("items", i.items.asJson),
      ("total", Json.fromLong(i.total))
    )
  }

}
