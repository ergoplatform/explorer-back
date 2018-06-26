package org.ergoplatform.explorer.db.models

case class RawSearchBlock(
                           id: String,
                           height: Long,
                           timestamp: Long,
                           txsCount: Long,
                           minerAddress: String,
                           minerName: String,
                           blockSize: Long
                         )
