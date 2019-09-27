package org.ergoplatform.explorer.db.models

import io.circe.Json

final case class Input(boxId: String, txId: String, proofBytes: String, extension: Json = Json.Null)
