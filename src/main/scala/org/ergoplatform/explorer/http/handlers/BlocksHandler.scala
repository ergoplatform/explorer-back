package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import cats.data._
import cats.effect.IO
import cats.implicits.catsKernelStdOrderForString
import cats.syntax.all._
import org.ergoplatform.explorer.http.protocol.ItemsResponse
import org.ergoplatform.explorer.services.BlockService
import org.ergoplatform.explorer.utils.{Paging, Sorting}


class BlocksHandler(bs: BlockService[IO]) extends RouteHandler {

  import BlocksHandler._

  private val oneDayMillis: Long = 24L * 60L * 60L * 1000L

  val route: Route = pathPrefix("blocks") {
    getBlockById ~ getBlocks
  }

  def getBlockById: Route = (get & base16Segment) { base58String =>
    val f = bs.getBlock(base58String).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

  def getBlockByD: Route = (pathPrefix("byD") & bigIntSegment & get) { bigInt =>
    val f = bs.getBlockByD(bigInt.toString()).unsafeToFuture()
    onSuccess(f) { info => complete(info) }
  }

  def getBlocks: Route = (get & paging & sorting(sortByFieldMappings, Some("height")) & startEndDate) {
    (o, l, field, so, start, end) =>
      val sTs = start.getOrElse(0L)
      val eTs = end.getOrElse(System.currentTimeMillis() + oneDayMillis)
      val p = Paging(offset = o, limit = l)
      val s = Sorting(sortBy = field, order = so)
      val items = bs.getBlocks(p, s, sTs, eTs)
      val count = bs.count(sTs, eTs)
      val itemsResponse = (items, count).parMapN(ItemsResponse.apply)
      itemsResponse
  }

}

object BlocksHandler {

  val sortByFieldMappings: NonEmptyMap[String, String] = NonEmptyMap.of(
    "height" -> "height",
    "timestamp" -> "timestamp",
    "transactionscount" -> "txs_count",
    "size" -> "block_size",
    "miner" -> "miner_name",
    "difficulty" -> "difficulty",
    "minerreward" -> "miner_reward"
  )

}
