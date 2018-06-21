package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor}

case class NodeFullBlock(header: NodeHeader, bt: NodeBlockTransactions, adProofs: Option[NodeAdProofs])

object NodeFullBlock {

  implicit val decoder: Decoder[NodeFullBlock] = (c: HCursor) => for {
    header <- c.downField("header").as[NodeHeader]
    bt <- c.downField("blockTransactions").as[NodeBlockTransactions]
    adProofs <- c.downField("adProofs").as[NodeAdProofs] match {
      case Left(_) => Right(None)
      case Right(proofs) => Right(Some(proofs))
    }
  } yield NodeFullBlock(header, bt, adProofs)
}
