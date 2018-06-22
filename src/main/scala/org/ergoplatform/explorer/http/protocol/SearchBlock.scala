package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{BlockInfo, Header}

case class SearchBlock(id: String,
                       height: Long,
                       timestamp: Long,
                       transactionsCount: Long,
                       miner: MinerInfo,
                       size: Long)

object SearchBlock {

  def fromHeader(h: Header, info: BlockInfo): SearchBlock = SearchBlock(
    id = h.id,
    height = h.height,
    timestamp = h.timestamp,
    transactionsCount = info.txsCount,
    miner = MinerInfo(info.minerAddress, info.minerName),
    size = info.blockSize
  )

  implicit val encoderSearchBlock: Encoder[SearchBlock] = (b: SearchBlock) => Json.obj(
    "id" -> Json.fromString(b.id),
    "height" -> Json.fromLong(b.height),
    "timestamp" -> Json.fromLong(b.timestamp),
    "transactionsCount" -> Json.fromLong(b.transactionsCount),
    "miner" -> b.miner.asJson,
    "size" -> Json.fromLong(b.size)
  )
}
