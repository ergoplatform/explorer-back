package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import cats.data._
import cats.effect.IO
import cats.implicits.catsKernelStdOrderForString
import cats.syntax.all._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.ergoplatform.explorer.http.directives.CommonDirectives
import org.ergoplatform.explorer.http.protocol.ItemsResponse
import org.ergoplatform.explorer.services.BlockService
import org.ergoplatform.explorer.utils.{Paging, Sorting}


class BlocksHandler(bs: BlockService[IO]) extends FailFastCirceSupport with CommonDirectives {

  import BlocksHandler._

  val route = pathPrefix("blocks") {
    getBlockById ~ getBlocks
  }

  val getBlockById = (get & base16Segment) { base58String =>
    val f = bs.getBlock(base58String).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

  val getBlocks = (get & paging & sorting(sortByFieldMappings, Some("height")) & startEndDate) {
    (o, l, field, so, start, end) =>
      val sTs = start.getOrElse(0L)
      val eTs = end.getOrElse(System.currentTimeMillis())
      val p = Paging(offset = o, limit = l)
      val s = Sorting(sortBy = field, order = so)
      val items = bs.getBlocks(p, s, sTs, eTs)
      val count = bs.count(sTs, eTs)
      val f = (items, count).parMapN { case (i, c) => ItemsResponse(i, c) }.unsafeToFuture()
      onSuccess(f) { info => complete(info) }
  }

}

object BlocksHandler {

  val sortByFieldMappings: NonEmptyMap[String, String] = NonEmptyMap.of(
    "height" -> "height",
    "timestamp" -> "ts",
    "transactionscount" -> "tx_count",
    "size" -> "block_size",
    "miner" -> "miner_name"
  )
}
