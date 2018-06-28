package org.ergoplatform.explorer.db.models

case class InputWithOutputInfo(input: Input, value: Long, outputTxId: String, address: String)
