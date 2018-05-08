package org.ergoplatform.explorer.models

case class Block(
                  id: String,
                  parentId: String,
                  version: Short,
                  height: Int,
                  adProofsRoot: String,
                  stateRoot: String,
                  transactionsRoot: String,
                  timestamp: Long,
                  nBits: Long,
                  nonce: Long,
                  votes: List[Byte] = List.empty[Byte],
                  equihashSolution: List[Int] = List.empty[Int]
                )

