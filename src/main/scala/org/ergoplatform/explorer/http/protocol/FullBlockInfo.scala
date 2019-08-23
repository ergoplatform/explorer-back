package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models._
import org.ergoplatform.explorer.db.models.composite.{ExtendedInput, ExtendedOutput}

final case class FullBlockInfo(
  headerInfo: HeaderInfo,
  transactionsInfo: List[TransactionInfo],
  extension: BlockExtension,
  adProof: Option[AdProofInfo]
)

object FullBlockInfo {

  /** Creating info model from related db entities
    */
  def apply(
    h: Header,
    txs: List[Transaction],
    confirmations: List[(String, Long)],
    inputs: List[ExtendedInput],
    outputs: List[ExtendedOutput],
    extension: BlockExtension,
    adProof: Option[AdProof],
    blockSize: Long
  ): FullBlockInfo = {
    val txsInfo = TransactionInfo.extractInfo(txs, confirmations, inputs, outputs)
    val headerInfo = HeaderInfo(h, blockSize)
    val adProofInfo = adProof.map { AdProofInfo.apply }
    new FullBlockInfo(headerInfo, txsInfo, extension, adProofInfo)
  }

  implicit val encoder: Encoder[FullBlockInfo] = { fb =>
    Json.obj(
      "header"            -> fb.headerInfo.asJson,
      "blockTransactions" -> fb.transactionsInfo.asJson,
      "extension"         -> fb.extension.asJson,
      "adProofs"          -> fb.adProof.map(_.proofBytes).asJson
    )
  }

}
