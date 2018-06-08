package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.Header

case class SearchBlock(id: String,
                       height: Int,
                       timestamp: Long,
                       transactionsCount: Int,
                       miner: MinerInfo,
                       size: Long)

object SearchBlock {

  def fromHeader(h: Header, txsCount: Int): SearchBlock = SearchBlock(
    id = h.id,
    height = h.height,
    timestamp = h.timestamp,
    transactionsCount = txsCount,
    miner = MinerInfo(h.minerAddress, h.minerName),
    size = h.blockSize
  )

  implicit val encoderSearchBlock: Encoder[SearchBlock] = (b: SearchBlock) => Json.obj(
    "id" -> Json.fromString(b.id),
    "height" -> Json.fromInt(b.height),
    "timestamp" -> Json.fromLong(b.timestamp),
    "transactionsCount" -> Json.fromInt(b.transactionsCount),
    "miner" -> b.miner.asJson,
    "size" -> Json.fromLong(b.size)
  )
}
