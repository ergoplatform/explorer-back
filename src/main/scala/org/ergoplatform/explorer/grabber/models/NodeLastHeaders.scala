package org.ergoplatform.explorer.grabber.models

import io.circe.Decoder

case class NodeLastHeaders(headers: List[NodeHeader])

object NodeLastHeaders {
  implicit val decoder: Decoder[NodeLastHeaders] = Decoder.decodeList[NodeHeader].emap(l => Right(NodeLastHeaders(l)))
}