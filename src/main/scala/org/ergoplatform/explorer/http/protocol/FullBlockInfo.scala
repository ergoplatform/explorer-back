package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models._

case class FullBlockInfo(headerInfo: HeaderInfo, transactionsInfo: List[TransactionInfo], adProof: Option[AdProofInfo])

object FullBlockInfo {

  /**
    * Creating info model from related db entities
    */
  def apply(h: Header,
            txs: List[Transaction],
            inputs: List[InputWithValue],
            outputs: List[SpentOutput],
            adProof: Option[AdProof]): FullBlockInfo = {

    val txsInfo = TransactionInfo.extractInfo(txs, inputs, outputs)
    val headerInfo = HeaderInfo(h)
    val adProofInfo = adProof.map{ AdProofInfo.apply }
    new FullBlockInfo(headerInfo, txsInfo, adProofInfo)
  }

  implicit val encoder: Encoder[FullBlockInfo] = (fb: FullBlockInfo) => Json.obj(
    "header" -> fb.headerInfo.asJson,
    "blockTransactions" -> fb.transactionsInfo.asJson,
    "adProofs" -> fb.adProof.map(_.proofBytes).asJson
  )
}
