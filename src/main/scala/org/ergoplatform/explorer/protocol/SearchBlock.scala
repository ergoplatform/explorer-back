package org.ergoplatform.explorer.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

case class SearchBlock(id: String,
                       height: Int,
                       timestamp: Long,
                       transactionsCount: Int,
                       miner: MinerInfo,
                       size: Int,
                       votes: String)

object SearchBlock {

  implicit val ecoderSearchBlock: Encoder[SearchBlock] = (b: SearchBlock) => Json.obj(
    ("id", Json.fromString(b.id)),
    ("height", Json.fromInt(b.height)),
    ("timestamp", Json.fromLong(b.timestamp)),
    ("transactionsCount", Json.fromInt(b.transactionsCount)),
    ("miner", b.miner.asJson),
    ("size", Json.fromInt(b.size)),
    ("votes", Json.fromString(b.votes))
  )
}
