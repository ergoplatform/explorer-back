package org.ergoplatform.explorer.grabber.protocol

import io.circe.{Decoder, HCursor}

case class ApiFullBlock(header: ApiHeader, transactions: ApiBlockTransactions, adProofs: Option[ApiAdProofs], size: Long)

object ApiFullBlock {

  implicit val decoder: Decoder[ApiFullBlock] = (c: HCursor) => for {
    header <- c.downField("header").as[ApiHeader]
    bt <- c.downField("blockTransactions").as[ApiBlockTransactions]
    adProofs <- c.downField("adProofs").as[ApiAdProofs] match {
      case Left(_) => Right(None)
      case Right(proofs) => Right(Some(proofs))
    }
    size <- c.downField("size").as[Long]
  } yield ApiFullBlock(header, bt, adProofs, size)

}
