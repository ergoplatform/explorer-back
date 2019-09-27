package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models.composite.RawSearchBlock

final case class SearchBlockInfo(
  id: String,
  height: Long,
  timestamp: Long,
  transactionsCount: Long,
  miner: MinerInfo,
  size: Long,
  difficulty: Long,
  minerReward: Long
)

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

  implicit val encoderSearchBlock: Encoder[SearchBlockInfo] = { bi =>
    Json.obj(
      "id"                -> Json.fromString(bi.id),
      "height"            -> Json.fromLong(bi.height),
      "timestamp"         -> Json.fromLong(bi.timestamp),
      "transactionsCount" -> Json.fromLong(bi.transactionsCount),
      "miner"             -> bi.miner.asJson,
      "size"              -> Json.fromLong(bi.size),
      "difficulty"        -> Json.fromLong(bi.difficulty),
      "minerReward"       -> Json.fromLong(bi.minerReward)
    )
  }

}
