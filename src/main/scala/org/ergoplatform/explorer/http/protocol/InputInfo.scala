package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Input

case class InputInfo(id: String, signature: String)

object InputInfo {

  def apply(i: Input): InputInfo = InputInfo(i.boxId, i.proofBytes)

  implicit val encoder: Encoder[InputInfo] = (i: InputInfo) => Json.obj(
    "id" -> Json.fromString(i.id),
    "signature" -> Json.fromString(i.signature)
  )
}
