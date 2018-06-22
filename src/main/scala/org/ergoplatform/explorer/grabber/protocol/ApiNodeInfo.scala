package org.ergoplatform.explorer.grabber.protocol

import io.circe._
import io.circe.generic.semiauto._

case class ApiNodeInfo(
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

object ApiNodeInfo {

  implicit val decoder: Decoder[ApiNodeInfo] = deriveDecoder
}
