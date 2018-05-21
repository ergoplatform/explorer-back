package org.ergoplatform.explorer.http.protocol

import com.google.common.primitives.Ints
import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{Header, Interlink}
import scorex.crypto.encode.Base58

case class HeaderInfo(id: String,
                      parentId: String,
                      version: Short,
                      height: Int,
                      adProofsRoot: String,
                      stateRoot: String,
                      transactionsRoot: String,
                      votes: String,
                      timestamp: Long,
                      nBits: Long,
                      extensionHash: String,
                      equihashSolution: String,
                      interlinks: List[String])

object HeaderInfo {

  import org.ergoplatform.explorer.utils.Converter._

  def apply(h: Header, interlinks: List[Interlink]): HeaderInfo = {
    val links = interlinks.filter(_.blockId == h.id).map(_.modifierId)
    val equihashSolutions: Array[Byte] = h.equihashSolution.flatMap { Ints.toByteArray }.toArray
    val equihashSolutionsString = Base58.encode(equihashSolutions)

    new HeaderInfo(
      from16to58(h.id),
      h.parentId,
      h.version,
      h.height,
      h.adProofsRoot,
      h.stateRoot,
      h.transactionsRoot,
      h.votes,
      h.timestamp,
      h.nBits,
      h.extensionHash,
      equihashSolutionsString,
      links
    )
  }

  implicit val encoder: Encoder[HeaderInfo] = (h: HeaderInfo) => Json.obj(
    ("id", Json.fromString(h.id)),
    ("parentId", Json.fromString(h.parentId)),
    ("version", Json.fromInt(h.version.toInt)),
    ("height", Json.fromInt(h.height)),
    ("interlinks", h.interlinks.asJson),
    ("adProofsRoot", Json.fromString(h.adProofsRoot)),
    ("stateRoot", Json.fromString(h.stateRoot)),
    ("transactionsRoot", Json.fromString(h.transactionsRoot)),
    ("nBits", Json.fromLong(h.nBits)),
    ("votes", Json.fromString(h.votes)),
    ("timestamp", Json.fromLong(h.timestamp)),
    ("extensionHash", Json.fromString(h.extensionHash)),
    ("equihashSolutions", Json.fromString(h.equihashSolution))
  )
}
