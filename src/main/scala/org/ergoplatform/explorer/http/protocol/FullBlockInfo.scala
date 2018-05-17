package org.ergoplatform.explorer.http.protocol

import io.circe.syntax._
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.db.models._
import scorex.crypto.encode.Base58

case class FullBlockInfo(headerInfo: HeaderInfo, transactionsInfo: List[TransactionInfo], adProofsInfo: String)

object FullBlockInfo {

  /**
    * Creating info model from related db entities
    */
  def apply(h: Header,
            i: List[Interlink],
            txs: List[Transaction],
            inputs: List[Input],
            outputs: List[Output]): FullBlockInfo = {

    val txsInfo = TransactionInfo.extractInfo(txs, inputs, outputs)
    val headerInfo = HeaderInfo(h, i)
    val adProofs = h.adProofs.map(v => Base58.encode(v)).getOrElse("")

    new FullBlockInfo(headerInfo, txsInfo, adProofs)
  }

  implicit val encoder: Encoder[FullBlockInfo] = (fb: FullBlockInfo) => Json.obj(
    ("header", fb.headerInfo.asJson),
    ("blockTransactions", fb.transactionsInfo.asJson),
    ("adProofs", Json.fromString(fb.adProofsInfo))
  )
}
