package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor}

case class NodeBlockTransactions(headerId: String, transactions: List[NodeTransaction])

object NodeBlockTransactions {

  implicit val decoder: Decoder[NodeBlockTransactions] = (c: HCursor) => for {
    headerId <- c.downField("headerId").as[String]
    transactions <- c.downField("transactions").as[List[NodeTransaction]]
  } yield NodeBlockTransactions(headerId, transactions)
}
