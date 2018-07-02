package org.ergoplatform.explorer.db.models

case class Header(
                   id: String,
                   parentId: String,
                   version: Short,
                   height: Long,
                   nBits: Long,
                   difficulty: Long,
                   timestamp: Long,
                   stateRoot: String,
                   adProofsRoot: String,
                   transactionsRoot: String,
                   extensionHash: String,
                   equihashSolutions: String,
                   interlinks: List[String],
                   mainChain: Boolean
                 )

