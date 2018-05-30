package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Input

case class InputInfo(id: String, outputId: String, signature: String)

object InputInfo {

  import org.ergoplatform.explorer.utils.Converter._

  def apply(i: Input): InputInfo = InputInfo(from16to58(i.id), from16to58(i.outputId), from16to58(i.signature))

  implicit val encoder: Encoder[InputInfo] = (i: InputInfo) => Json.obj(
    "id" -> Json.fromString(i.id),
    "outputId" -> Json.fromString(i.outputId),
    "signature" -> Json.fromString(i.signature)
  )
}
