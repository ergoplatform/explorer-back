package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.grabber.protocol.ApiTransaction
import org.ergoplatform.explorer.http.protocol.{
  MiniBlockInfo,
  OutputInfo,
  TransactionInfo,
  TransactionSummaryInfo
}
import org.ergoplatform.explorer.services.TransactionsService
import org.ergoplatform.explorer.utils.Paging

class TransactionsHandlerSpec extends HttpSpec {

  val infoResp =
    TransactionSummaryInfo(
      "test",
      0L,
      1L,
      2L,
      MiniBlockInfo("r", 5L),
      List.empty,
      List.empty
    )

  private val service = new TransactionsService[IO] {

    override def getUnconfirmedTxInfo(id: String): IO[ApiTransaction] = ???

    override def getTxInfo(id: String): IO[TransactionSummaryInfo] = IO.pure(infoResp)

    override def getTxsSince(height: Int, p: Paging): IO[List[TransactionInfo]] = ???

    override def getTxsByAddressId(
      addressId: String,
      p: Paging
    ): IO[List[TransactionInfo]] = ???

    override def countTxsByAddressId(addressId: String): IO[Long] = ???

    override def searchByIdSubstr(query: String): IO[List[String]] = ???

    override def getOutputById(id: String): IO[OutputInfo] = ???

    override def getOutputsByAddress(
      hash: String,
      unspentOnly: Boolean = false
    ): IO[List[OutputInfo]] = ???

    override def getOutputsByErgoTree(
      ergoTree: String,
      unspentOnly: Boolean = false
    ): IO[List[OutputInfo]] = IO.pure(List.empty)

    override def getOutputsByErgoTreeTemplate(
      ergoTree: String,
      unspentOnly: Boolean
    ): IO[List[OutputInfo]] = IO.pure(List.empty)

    override def submitTransaction(tx: Json): IO[Json] = ???

    override def getUnconfirmed: IO[List[ApiTransaction]] = ???

    override def getUnconfirmedByAddress(address: String): IO[List[ApiTransaction]] = ???
  }

  val route: Route = new TransactionsHandler(service).route

  it should "return tx info by id" in {
    Get("/transactions/0001111abb") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe infoResp.asJson
    }
  }

  it should "find outputs by ErgoTree" in {
    Get("/transactions/boxes/byErgoTree/31") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe Array.empty[OutputInfo].asJson
    }
  }

  it should "find unspent outputs by ErgoTree" in {
    Get("/transactions/boxes/byErgoTree/unspent/31") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe Array.empty[OutputInfo].asJson
    }
  }

  it should "find outputs by ErgoTree template" in {
    Get("/transactions/boxes/byErgoTreeTemplate/31") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe Array.empty[OutputInfo].asJson
    }
  }

  it should "find unspent outputs by ErgoTree template" in {
    Get("/transactions/boxes/byErgoTreeTemplate/unspent/31") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe Array.empty[OutputInfo].asJson
    }
  }
}
