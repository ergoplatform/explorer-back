package org.ergoplatform.explorer.models

case class Transaction(
                        id: String,
                        blockId: String,
                        inputs: List[Long] = List.empty[Long],
                        outputs: List[Long] = List.empty[Long]
                      )
