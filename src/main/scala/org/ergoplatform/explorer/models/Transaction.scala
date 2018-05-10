package org.ergoplatform.explorer.models

case class Transaction(
                        id: String,
                        blockId: String,
                        isCoinbase: Boolean
                      ) extends Entity[String]
