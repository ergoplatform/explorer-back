package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Header

case class HeaderInfo(
                       id: String,
                       parentId: String,
                       version: Short,
                       height: Long,
                       adProofsRoot: String,
                       stateRoot: String,
                       transactionsRoot: String,
                       timestamp: Long,
                       nBits: Long,
                       size: Long,
                       extensionHash: String,
                       equihashSolution: String,
                       interlinks: List[String]
                     )

object HeaderInfo {

  def apply(h: Header, size: Long): HeaderInfo = {

    new HeaderInfo(
      h.id,
      h.parentId,
      h.version,
      h.height,
      h.adProofsRoot,
      h.stateRoot,
      h.transactionsRoot,
      h.timestamp,
      h.nBits,
      size,
      h.extensionHash,
      h.equihashSolutions,
      h.interlinks
    )
  }

  implicit val encoder: Encoder[HeaderInfo] = (h: HeaderInfo) => Json.obj(
    "id" -> Json.fromString(h.id),
    "parentId" -> Json.fromString(h.parentId),
    "version" -> Json.fromInt(h.version.toInt),
    "height" -> Json.fromLong(h.height),
    "interlinks" -> h.interlinks.asJson,
    "adProofsRoot" -> Json.fromString(h.adProofsRoot),
    "stateRoot" -> Json.fromString(h.stateRoot),
    "transactionsRoot" -> Json.fromString(h.transactionsRoot),
    "nBits" -> Json.fromLong(h.nBits),
    "size" -> Json.fromLong(h.size),
    "timestamp" -> Json.fromLong(h.timestamp),
    "extensionHash" -> Json.fromString(h.extensionHash),
    "equihashSolutions" -> Json.fromString(h.equihashSolution)
  )
}
