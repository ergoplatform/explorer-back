package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.protocol._
import org.ergoplatform.explorer.services.BlockService
import org.ergoplatform.explorer.utils.{Paging, Sorting}

class BlocksHandlerSpec extends HttpSpec {

  val headerInfo = HeaderInfo("1", "2", 1: Short, 2L, 100L, "a", "b", "c", 0L, 0L, 0L, "d", "e", List("g", "h"))

  val txs = List(
    TransactionInfo("test1", 0L, 1L, List.empty, List.empty),
    TransactionInfo("test2", 0L, 1L, List.empty, List.empty),
    TransactionInfo("test3", 0L, 1L, List.empty, List.empty)
  )

  val block = BlockSummaryInfo(
    info = FullBlockInfo(headerInfo, txs, Some(AdProofInfo("a", "b", "c"))),
    references = BlockReferencesInfo("a", Some("b"))

  )

  val minerInfo = MinerInfo("address", "name")

  val blocks = List(
    SearchBlockInfo("1", 1L, 2L, 3L, minerInfo, 4L, 5L, 6L),
    SearchBlockInfo("2", 1L, 2L, 3L, minerInfo, 4L, 5L, 6L),
    SearchBlockInfo("3", 1L, 2L, 3L, minerInfo, 4L, 5L, 6L)
  )


  val blockServiceStub = new BlockService[IO] {
    override def getBlock(id: String): IO[BlockSummaryInfo] = IO.pure(block)

    override def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): IO[List[SearchBlockInfo]] = IO.pure(blocks)

    override def count(startTs: Long, endTs: Long): IO[Long] = IO.pure(3L)

    override def searchById(query: String): IO[List[SearchBlockInfo]] = IO.pure(blocks)
  }

  val route = new BlocksHandler(blockServiceStub).route

  it should "get block by id" in {
    Get("/blocks/0001111abcbbbc") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe block.asJson
    }
  }

  it should "gel blocks" in {
    Get("/blocks") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe ItemsResponse(blocks, 3L).asJson
    }
  }
}
