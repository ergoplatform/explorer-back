package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.effect.IO
import cats.syntax.all._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.http.protocol.ItemsResponse
import org.ergoplatform.explorer.services.BlockService
import org.ergoplatform.explorer.utils.{Paging, Sorting}


class BlocksHandler(bs: BlockService[IO]) extends FailFastCirceSupport with CommonDirectives {

  val route = pathPrefix("blocks") {
    getBlockById ~ getBlocks
  }

  val getBlockById = (get & base58IdPath) { base58String =>
    val f = bs.getBlock(base58String).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

  val getBlocks = (get & paging & sorting) { (o, l, field, so) =>
    val p = Paging(offset = o, limit = l)
    val s = Sorting(sortBy = field, order = so)
    val items = bs.getBlocks(p, s)
    val count = bs.count()
    val f = (items, count).parMapN((i, c) => ItemsResponse(i, c)).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

}
