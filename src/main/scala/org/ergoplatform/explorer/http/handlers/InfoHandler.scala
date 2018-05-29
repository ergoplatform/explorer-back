package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.protocol.BlockchainInfo

class InfoHandler extends FailFastCirceSupport {

  val stub = BlockchainInfo("1.0.0", 1000003L, 10003243L, 100302L, 415434144L)

  val route = (pathPrefix("info") & get) { complete(stub) }

}
