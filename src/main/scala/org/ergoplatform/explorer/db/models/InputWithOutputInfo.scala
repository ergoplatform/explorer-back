package org.ergoplatform.explorer.db.models

case class InputWithOutputInfo(input: Input, value: Option[Long], outputTxId: Option[String], address: Option[String])
