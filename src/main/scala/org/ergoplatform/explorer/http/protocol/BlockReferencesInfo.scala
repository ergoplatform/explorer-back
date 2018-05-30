package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.utils.Converter._

case class BlockReferencesInfo(previousId: String, nextId: Option[String])

object BlockReferencesInfo {

  def apply(previousId: String, nextId: Option[String]): BlockReferencesInfo = new BlockReferencesInfo(
    from16to58(previousId),
    nextId.map(from16to58)
  )

  implicit val encoder: Encoder[BlockReferencesInfo] = (br: BlockReferencesInfo) => Json.obj(
    ("previousId", Json.fromString(br.previousId)),
    ("nextId", br.nextId.fold(Json.Null) { Json.fromString })
  )
}
