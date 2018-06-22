package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

case class BlockReferencesInfo(previousId: String, nextId: Option[String])

object BlockReferencesInfo {

  def apply(previousId: String, nextId: Option[String]): BlockReferencesInfo = new BlockReferencesInfo(
    previousId,
    nextId
  )

  implicit val encoder: Encoder[BlockReferencesInfo] = (br: BlockReferencesInfo) => Json.obj(
    "previousId" -> Json.fromString(br.previousId),
    "nextId" ->  br.nextId.asJson
  )
}
