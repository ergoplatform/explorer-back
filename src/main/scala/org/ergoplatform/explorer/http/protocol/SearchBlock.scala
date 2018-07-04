package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{BlockInfo, Header, RawSearchBlock}

case class SearchBlock(id: String,
                       height: Long,
                       timestamp: Long,
                       transactionsCount: Long,
                       miner: MinerInfo,
                       size: Long)

object SearchBlock {

  def fromRawSearchBlock(b: RawSearchBlock): SearchBlock = {
    val minerName = b.minerName.getOrElse(b.minerAddress.takeRight(8))
    SearchBlock(
      id = b.id,
      height = b.height,
      timestamp = b.timestamp,
      transactionsCount = b.txsCount,
      miner = MinerInfo(b.minerAddress, minerName),
      size = b.blockSize
    )
  }

  implicit val encoderSearchBlock: Encoder[SearchBlock] = (b: SearchBlock) => Json.obj(
    "id" -> Json.fromString(b.id),
    "height" -> Json.fromLong(b.height),
    "timestamp" -> Json.fromLong(b.timestamp),
    "transactionsCount" -> Json.fromLong(b.transactionsCount),
    "miner" -> b.miner.asJson,
    "size" -> Json.fromLong(b.size)
  )
}
