package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.{BlockInfo, Header, RawSearchBlock}

case class SearchBlockInfo(id: String,
                           height: Long,
                           timestamp: Long,
                           transactionsCount: Long,
                           miner: MinerInfo,
                           size: Long,
                           difficulty: Long,
                           minerReward: Long)

object SearchBlockInfo {

  def fromRawSearchBlock(b: RawSearchBlock): SearchBlockInfo = {
    val minerName = b.minerName.getOrElse(b.minerAddress.takeRight(8))
    SearchBlockInfo(
      id = b.id,
      height = b.height,
      timestamp = b.timestamp,
      transactionsCount = b.txsCount,
      miner = MinerInfo(b.minerAddress, minerName),
      size = b.blockSize,
      difficulty = b.difficulty,
      minerReward = b.minerReward
    )
  }

  implicit val encoderSearchBlock: Encoder[SearchBlockInfo] = (b: SearchBlockInfo) => Json.obj(
    "id" -> Json.fromString(b.id),
    "height" -> Json.fromLong(b.height),
    "timestamp" -> Json.fromLong(b.timestamp),
    "transactionsCount" -> Json.fromLong(b.transactionsCount),
    "miner" -> b.miner.asJson,
    "size" -> Json.fromLong(b.size),
    "difficulty" -> Json.fromLong(b.difficulty),
    "minerReward" -> Json.fromLong(b.minerReward)
  )
}
