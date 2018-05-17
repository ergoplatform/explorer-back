package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.services.BlockService


class BlocksHandler(bs: BlockService[IO]) extends FailFastCirceSupport with CommonDirectives {

  val route = pathPrefix("blocks") {
    getBlockById
  }

  val getBlockById = (get & base58IdPath) { base58String =>
    val f = bs.getBlock(base58String).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

}
