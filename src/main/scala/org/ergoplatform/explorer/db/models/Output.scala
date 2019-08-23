package org.ergoplatform.explorer.db.models

import io.circe.Json

final case class Output(
  boxId: String,
  txId: String,
  value: Long,
  creationHeight: Int,
  index: Int,
  ergoTree: String,
  address: String,
  additionalRegisters: Json = Json.Null,
  timestamp: Long
)
