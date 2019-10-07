package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.{Route, ValidationRejection}
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.http.protocol._
import org.ergoplatform.explorer.services.{AddressesService, BlockService, MinerService, TransactionsService}
import org.ergoplatform.explorer.utils.{Paging, Sorting}

class SearchHandlerSpec extends HttpSpec {

  val miner = MinerInfo("mock.miner.address", "Mock miner")
  val block = List(SearchBlockInfo("123123", 1, System.currentTimeMillis(), 42, miner, 100, 0L, 0L))
  val transaction = List("fff123")
  val address = List("add123")

  val searchInfo = SearchInfo(block, transaction, address)

  val blockService: BlockService[IO] = new BlockService[IO] {
    override def getBlockByD(d: String): IO[BlockSummaryInfo] = ???
    override def getBlock(id: String): IO[BlockSummaryInfo] = ???
    override def getBlocks(
      p: Paging,
      s: Sorting,
      start: Long,
      end: Long
    ): IO[List[SearchBlockInfo]] = ???
    override def count(startTs: Long, endTs: Long): IO[Long] = ???
    override def searchById(query: String): IO[List[SearchBlockInfo]] = IO.pure(block)
  }

  val transactionService: TransactionsService[IO] = new TransactionsService[IO] {
    override def getUnconfirmedTxInfo(id: String): IO[ApiTransaction] = ???
    override def getTxsSince(height: Int, p: Paging): IO[List[TransactionInfo]] = ???
    override def getTxInfo(id: String): IO[TransactionSummaryInfo] = ???
    override def getTxsByAddressId(addressId: String, p: Paging): IO[List[TransactionInfo]] = ???
    override def countTxsByAddressId(addressId: String): IO[Long] = ???
    override def searchByIdSubstr(query: String): IO[List[String]] = IO.pure(transaction)
    override def getOutputById(id: String): IO[OutputInfo] = ???
    override def getOutputsByAddress(
      hash: String,
      unspentOnly: Boolean = false
    ): IO[List[OutputInfo]] = ???
    override def getOutputsByErgoTree(
      ergoTree: String,
      unspentOnly: Boolean = false
    ): IO[List[OutputInfo]] = ???
    override def submitTransaction(tx: Json): IO[Json] = ???
    override def getUnconfirmed: IO[List[ApiTransaction]] = ???
    override def getUnconfirmedByAddress(address: String): IO[List[ApiTransaction]] = ???
  }

  val addressService: AddressesService[IO] = new AddressesService[IO] {
    override def getAddressInfo(addressId: String): IO[AddressInfo] = ???
    override def searchById(query: String): IO[List[String]] = IO.pure(address)
    override def holdersAddresses(
      assetId: String,
      p: Paging
    ): IO[List[String]] = ???
  }

  val minerService: MinerService[IO] = (_: String) => IO.pure(address)

  val route: Route =
    new SearchHandler(blockService, transactionService, addressService, minerService).route

  it should "return result" in {
    Get("/search?query=test44") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe searchInfo.asJson
    }

    Get("/search?query=test") ~> route ~> check {
      rejection shouldBe ValidationRejection(
        "'query' param should be at least 5 characters long",
        None
      )
    }
  }

}
