package org.ergoplatform.explorer.db.models

import io.circe.Json

final case class BlockExtension(headerId: String, digest: String, fields: Json)
