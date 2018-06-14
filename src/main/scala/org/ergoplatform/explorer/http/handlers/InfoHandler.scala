package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.protocol.BlockchainInfo
import org.ergoplatform.explorer.services.StatsService

class InfoHandler(ss: StatsService[IO]) extends FailFastCirceSupport {

  val emptyInfoResponse = BlockchainInfo("0.0.0", 0L, 0L, 0L)

  val route = (pathPrefix("info") & get) {
    val f = ss.findBlockchainInfo.unsafeToFuture()
    onSuccess(f) {
      case Some(info) => complete(info)
      case None => complete(emptyInfoResponse)
    }
  }

}
