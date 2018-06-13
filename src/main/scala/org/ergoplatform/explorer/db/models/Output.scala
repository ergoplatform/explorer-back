package org.ergoplatform.explorer.db.models

case class Output(id: String,
                  txId: String,
                  value: Long,
                  script: String,
                  hash: String)
