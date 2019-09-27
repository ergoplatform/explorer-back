package org.ergoplatform.explorer.db.models.composite

import org.ergoplatform.explorer.db.models.Input

final case class ExtendedInput(
  input: Input,
  value: Option[Long],
  outputTxId: Option[String],
  address: Option[String]
)
