package org.ergoplatform.explorer.http.protocol

import com.google.common.primitives.Ints
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{Header, Interlink}
import scorex.crypto.encode.Base16

case class HeaderInfo(id: String,
                      parentId: String,
                      version: Short,
                      height: Int,
                      adProofsRoot: String,
                      stateRoot: String,
                      transactionsRoot: String,
                      timestamp: Long,
                      nBits: Long,
                      size: Long,
                      extensionHash: String,
                      equihashSolution: String,
                      interlinks: List[String])

object HeaderInfo {

  def apply(h: Header, interlinks: List[Interlink]): HeaderInfo = {
    val links = interlinks.filter(_.blockId == h.id).map(_.modifierId)
    val equihashSolutions: Array[Byte] = h.equihashSolution.flatMap { Ints.toByteArray }.toArray
    val equihashSolutionsString = Base16.encode(equihashSolutions)

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
      h.blockSize,
      h.extensionHash,
      equihashSolutionsString,
      links
    )
  }

  implicit val encoder: Encoder[HeaderInfo] = (h: HeaderInfo) => Json.obj(
    "id" -> Json.fromString(h.id),
    "parentId" -> Json.fromString(h.parentId),
    "version" -> Json.fromInt(h.version.toInt),
    "height" -> Json.fromInt(h.height),
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
