package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}

case class SearchBlock(id: String,
                       height: Int,
                       timestamp: Long,
                       transactionsCount: Int,
                       miner: MinerInfo,
                       size: Int,
                       votes: String)

object SearchBlock {

  implicit val encoderSearchBlock: Encoder[SearchBlock] = (b: SearchBlock) => Json.obj(
    ("id", Json.fromString(b.id)),
    ("height", Json.fromInt(b.height)),
    ("timestamp", Json.fromLong(b.timestamp)),
    ("transactionsCount", Json.fromInt(b.transactionsCount)),
    ("miner", b.miner.asJson),
    ("size", Json.fromInt(b.size)),
    ("votes", Json.fromString(b.votes))
  )
}
