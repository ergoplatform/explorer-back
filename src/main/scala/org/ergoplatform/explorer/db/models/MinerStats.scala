package org.ergoplatform.explorer.db.models

case class MinerStats(
                       minerAddress: String,
                       totalDifficulties: Long,
                       totalTime: Long,
                       blocksMined: Long,
                       minerName: Option[String]
                     ) {
  val printableName: String = minerName.getOrElse(minerAddress.takeRight(8))
}
