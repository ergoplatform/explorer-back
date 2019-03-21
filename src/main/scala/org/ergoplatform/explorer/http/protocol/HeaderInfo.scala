package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Header
import org.ergoplatform.explorer.grabber.protocol.ApiPowSolutions
import scorex.util.encode.Base16

case class HeaderInfo(
                       id: String,
                       parentId: String,
                       version: Short,
                       height: Long,
                       difficulty: Long,
                       adProofsRoot: String,
                       stateRoot: String,
                       transactionsRoot: String,
                       timestamp: Long,
                       nBits: Long,
                       size: Long,
                       extensionHash: String,
                       powSolutions: ApiPowSolutions,
                       votes: String
                     )

object HeaderInfo {

  def apply(h: Header, size: Long): HeaderInfo = {
    val powSolutions = ApiPowSolutions(h.minerPk, h.w, h.n, h.d)
    new HeaderInfo(
      h.id,
      h.parentId,
      h.version,
      h.height,
      h.difficulty,
      h.adProofsRoot,
      h.stateRoot,
      h.transactionsRoot,
      h.timestamp,
      h.nBits,
      size,
      h.extensionHash,
      powSolutions,
      h.votes
    )
  }

  implicit val encoder: Encoder[HeaderInfo] = { hi: HeaderInfo =>
    Json.obj(
      "id" -> Json.fromString(hi.id),
      "parentId" -> Json.fromString(hi.parentId),
      "version" -> Json.fromInt(hi.version.toInt),
      "height" -> Json.fromLong(hi.height),
      "difficulty" -> Json.fromLong(hi.difficulty),
      "adProofsRoot" -> Json.fromString(hi.adProofsRoot),
      "stateRoot" -> Json.fromString(hi.stateRoot),
      "transactionsRoot" -> Json.fromString(hi.transactionsRoot),
      "nBits" -> Json.fromLong(hi.nBits),
      "timestamp" -> Json.fromLong(hi.timestamp),
      "extensionHash" -> Json.fromString(hi.extensionHash),
      "powSolutions" -> hi.powSolutions.asJson,
      "votes" -> expandVotes(hi.votes).asJson,
      "size" -> Json.fromLong(hi.size)
    )
  }

  private def expandVotes(votesHex: String) = {
    val defaultVotes = (0: Byte, 0: Byte, 0: Byte)
    val paramsQty = 3
    Base16.decode(votesHex)
      .map {
        case votes if votes.length == paramsQty => (votes(0): Byte, votes(1): Byte, votes(2): Byte)
        case _ => defaultVotes
      }
      .getOrElse(defaultVotes)
  }

}
