package org.ergoplatform.explorer.http.protocol

import io.circe.{Encoder, Json}
import io.circe.syntax._

final case class BlockSummaryInfo(info: FullBlockInfo, references: BlockReferencesInfo)

object BlockSummaryInfo {

  implicit val encoder: Encoder[BlockSummaryInfo] = { bs =>
    Json.obj(
      ("block", bs.info.asJson),
      ("references", bs.references.asJson)
    )
  }

}
