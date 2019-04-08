package org.ergoplatform.explorer.db.models

import io.circe.Json

case class Output(
                   boxId: String,
                   txId: String,
                   value: Long,
                   index: Int,
                   proposition: String,
                   hash: String,
                   assets: Json = Json.Null,
                   additionalRegisters: Json = Json.Null,
                   timestamp: Long
                 )
