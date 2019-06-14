package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor}
import org.ergoplatform.explorer.db.models.BlockExtension

case class ApiFullBlock(header: ApiHeader,
                        transactions: ApiBlockTransactions,
                        extension: BlockExtension,
                        adProofs: Option[ApiAdProofs],
                        size: Long)

object ApiFullBlock {

  implicit val decoder: Decoder[ApiFullBlock] = { c: HCursor =>
    for {
      header <- c.downField("header").as[ApiHeader]
      transactions <- c.downField("blockTransactions").as[ApiBlockTransactions]
      extension <- c.downField("extension").as[BlockExtension]
      adProofs <- c.downField("adProofs").as[ApiAdProofs] match {
        case Left(_) => Right(None)
        case Right(proofs) => Right(Some(proofs))
      }
      size <- c.downField("size").as[Long]
    } yield ApiFullBlock(header, transactions, extension, adProofs, size)
  }

}
