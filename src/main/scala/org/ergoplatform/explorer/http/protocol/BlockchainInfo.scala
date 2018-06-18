package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}

case class BlockchainInfo(version: String,
                          supply: Long,
                          averageTransactionPerBlock: Long,
                          hashRate: Long)

object BlockchainInfo {

  implicit val encoder: Encoder[BlockchainInfo] = (i: BlockchainInfo) => Json.obj(
    "version" -> Json.fromString(i.version),
    "supply" -> Json.fromLong(i.supply),
    "transactionAverage" -> Json.fromLong(i.averageTransactionPerBlock),
    "hashRate" -> Json.fromLong(i.hashRate)
  )

  val empty = BlockchainInfo("0.0.0", 0L, 0L, 0L)
}
