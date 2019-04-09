package org.ergoplatform.explorer.db.models

final case class ExtendedOutput(output: Output, spentTxId: Option[String], mainChain: Boolean)
