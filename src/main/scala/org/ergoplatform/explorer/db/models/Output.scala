package org.ergoplatform.explorer.db.models

case class Output(id: String,
                  txId: String,
                  value: Long,
                  spent: Boolean,
                  script: String,
                  hash: String) extends Entity[String]
