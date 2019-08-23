package org.ergoplatform.explorer.db.models.composite

final case class MinerStats(
  minerAddress: String,
  totalDifficulties: Long,
  totalTime: Long,
  blocksMined: Long,
  minerName: Option[String]
) {
  val verboseName: String = minerName.getOrElse(minerAddress.takeRight(8))
}
