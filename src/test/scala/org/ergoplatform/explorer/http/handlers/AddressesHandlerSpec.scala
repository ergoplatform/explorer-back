package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.protocol._
import org.ergoplatform.explorer.services.{AddressesService, TransactionsService}
import org.ergoplatform.explorer.utils.Paging

class AddressesHandlerSpec extends HttpSpec {

  val addressInfo = AddressInfo("test", 1L, 2L, 3L)
  val txs = List(
    TransactionInfo("test1", 0L, 1L, List.empty, List.empty),
    TransactionInfo("test2", 0L, 1L, List.empty, List.empty),
    TransactionInfo("test3", 0L, 1L, List.empty, List.empty)
  )

  val addressServiceStub = new AddressesService[IO] {
    override def getAddressInfo(addressId: String): IO[AddressInfo] = IO.pure(addressInfo)

    override def searchById(query: String): IO[List[String]] = IO.pure(List("test1", "test2"))
  }

  val txServiceStub = new TransactionsService[IO] {

    override def getTxInfo(id: String): IO[TransactionSummaryInfo] = IO.pure(
      TransactionSummaryInfo("test", 0L, 1L, 2L, MiniBlockInfo("r", 5L), List.empty, List.empty)
    )

    override def getTxsByAddressId(addressId: String, p: Paging): IO[List[TransactionInfo]] = IO.pure(txs)

    override def countTxsByAddressId(addressId: String): IO[Long] = IO.pure(3L)

    override def searchById(query: String): IO[List[String]] = IO.pure(List("test1", "test2"))

    def getOutputsByAddress(hash: String, unspentOnly: Boolean = false): IO[List[OutputInfo]] = ???

    def getOutputsByErgoTree(ergoTree: String, unspentOnly: Boolean = false): IO[List[OutputInfo]] = ???
  }

  val route = new AddressesHandler(addressServiceStub, txServiceStub).route

  it should "get address by base58 id" in {
    Get("/addresses/tJPvQ5UwUKLtgaB3iCALNcjpUQJLcFJNiudZpiAHNtLVFScyubHkoz") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe addressInfo.asJson
    }
  }

  it should "get txs by address id" in {
    Get("/addresses/tJPvQ5UwUKLtgaB3iCALNcjpUQJLcFJNiudZpiAHNtLVFScyubHkoz/transactions") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe ItemsResponse(txs, 3L).asJson
    }
  }

}
