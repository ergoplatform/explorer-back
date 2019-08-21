package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.db.models.BlockExtension
import org.ergoplatform.explorer.grabber.protocol.ApiPowSolutions
import org.ergoplatform.explorer.http.protocol._
import org.ergoplatform.explorer.services.BlockService
import org.ergoplatform.explorer.utils.{Paging, Sorting}

class BlocksHandlerSpec extends HttpSpec {

  val pow = ApiPowSolutions(
    "020dbc0e4f5f57235250f840988e025e8ef54348cc6ae3f2e3c3a4cc88724295d0",
    "0320514b1620dedb092edefbbe8d883289caceccb2f23707058396606f482ed650",
    "00000000000083ae",
    "549147274744846704056800281002663775202262031175081146646290287367723"
  )

  val headerInfo =
    HeaderInfo("1", "2", 1: Short, 2L, 100L, "a", "b", "c", 0L, 0L, 0L, "d", pow, "0000")

  val txs = List(
    TransactionInfo("test1", 0L, 1L, List.empty, List.empty),
    TransactionInfo("test2", 0L, 1L, List.empty, List.empty),
    TransactionInfo("test3", 0L, 1L, List.empty, List.empty)
  )

  val extension = BlockExtension(
    "019bbcdbce2f8859c1930dd2ccf6f887def1b82a67d9b4d09469b6826017126306",
    "011afcb9cc16fdebf4a211fffb1540153669071b1e6b1e7087afa14dfad896812c",
    Json.Null
  )

  val block = BlockSummaryInfo(
    info = FullBlockInfo(headerInfo, txs, extension, Some(AdProofInfo("a", "b", "c"))),
    references = BlockReferencesInfo("a", Some("b"))
  )

  val minerInfo = MinerInfo("address", "name")

  val blocks = List(
    SearchBlockInfo("1", 1L, 2L, 3L, minerInfo, 4L, 5L, 6L),
    SearchBlockInfo("2", 1L, 2L, 3L, minerInfo, 4L, 5L, 6L),
    SearchBlockInfo("3", 1L, 2L, 3L, minerInfo, 4L, 5L, 6L)
  )

  val blockServiceStub: BlockService[IO] = new BlockService[IO] {
    override def getBlockByD(d: String): IO[BlockSummaryInfo] = ???

    override def getBlock(id: String): IO[BlockSummaryInfo] = IO.pure(block)

    override def getBlocks(
      p: Paging,
      s: Sorting,
      start: Long,
      end: Long
    ): IO[List[SearchBlockInfo]] = IO.pure(blocks)

    override def count(startTs: Long, endTs: Long): IO[Long] = IO.pure(3L)

    override def searchById(query: String): IO[List[SearchBlockInfo]] = IO.pure(blocks)
  }

  val route: Route = new BlocksHandler(blockServiceStub).route

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
