package org.ergoplatform.explorer.grabber.models

import io.circe.{Decoder, HCursor}

case class NodeAdProofs(headerId: String, proofBytes: String, digest: String)

object NodeAdProofs {

  implicit val decoder: Decoder[NodeAdProofs] = (c: HCursor) => for {
    headerId <- c.downField("headerId").as[String]
    proofBytes <- c.downField("proofBytes").as[String]
    digest <- c.downField("digest").as[String]
  } yield NodeAdProofs(headerId, proofBytes, digest)
}
