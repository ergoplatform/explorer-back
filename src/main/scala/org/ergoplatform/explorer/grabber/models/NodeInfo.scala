package org.ergoplatform.explorer.grabber.models

import io.circe._, io.circe.generic.semiauto._

case class NodeInfo(
                     currentTime: Long,
                     name: String,
                     stateType: String,
                     difficulty: Long,
                     votes: String,
                     bestFullHeaderId: String,
                     bestHeaderId: String,
                     peersCount: Int,
                     unconfirmedCount: Int,
                     appVersion: String,
                     stateRoot: String,
                     previousFullHeaderId: String,
                     fullHeight: Long,
                     headersHeight: Long,
                     stateVersion: String,
                     fullBlocksScore: Long,
                     launchTime: Long,
                     headersScore: Long,
                     isMining: Boolean
                   )

object NodeInfo {

  implicit val decoder: Decoder[NodeInfo] = deriveDecoder
}
