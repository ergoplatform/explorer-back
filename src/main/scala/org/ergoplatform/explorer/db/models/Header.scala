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
                   minerPk: String,
                   w: String,
                   n: String,
                   d: String,
                   votes: String,
                   mainChain: Boolean
                 )

