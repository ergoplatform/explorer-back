package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.ValidationRejection
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.protocol._
import org.ergoplatform.explorer.services.{AddressesService, BlockService, MinerService, TransactionsService}
import org.ergoplatform.explorer.utils.{Paging, Sorting}

class SearchHandlerSpec extends HttpSpec {

  val miner = MinerInfo("mock.miner.address", "Mock miner")
  val block = List(SearchBlockInfo("123123", 1, System.currentTimeMillis(), 42, miner, 100, 0L, 0L))
  val transaction = List("fff123")
  val address = List("add123")

  val searchInfo = SearchInfo(block, transaction, address)

  val blockService = new BlockService[IO] {
    override def getBlock(id: String): IO[BlockSummaryInfo] = ???
    override def getBlocks(p: Paging, s: Sorting, start: Long, end: Long): IO[List[SearchBlockInfo]] = ???
    override def count(startTs: Long, endTs: Long): IO[Long] = ???
    override def searchById(query: String): IO[List[SearchBlockInfo]] = IO.pure(block)
  }

  val transactionService = new TransactionsService[IO] {
    override def getTxInfo(id: String): IO[TransactionSummaryInfo] = ???
    override def getTxsByAddressId(addressId: String, p: Paging): IO[List[TransactionInfo]] = ???
    override def countTxsByAddressId(addressId: String): IO[Long] = ???
    override def searchById(query: String): IO[List[String]] = IO.pure(transaction)
    override def getOutputById(id: String): IO[OutputInfo] = ???
    override def getOutputsByAddress(hash: String, unspentOnly: Boolean = false): IO[List[OutputInfo]] = ???
    override def getOutputsByErgoTree(ergoTree: String, unspentOnly: Boolean = false): IO[List[OutputInfo]] = ???
  }
  val addressService = new AddressesService[IO] {
    override def getAddressInfo(addressId: String): IO[AddressInfo] = ???
    override def searchById(query: String): IO[List[String]] = IO.pure(address)
  }
  val minerService = new MinerService[IO] {
    override def searchAddress(query: String): IO[List[String]] = IO.pure(address)
  }


  val route = new SearchHandler(blockService, transactionService, addressService, minerService).route

  it should "return result" in {
    Get("/search?query=test44") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe searchInfo.asJson
    }

    Get("/search?query=test") ~> route ~> check {
      rejection shouldBe ValidationRejection("'query' param should be at least 5 characters long", None)
    }
  }

}
