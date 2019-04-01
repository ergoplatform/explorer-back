package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.protocol.{MiniBlockInfo, OutputInfo, TransactionInfo, TransactionSummaryInfo}
import org.ergoplatform.explorer.services.TransactionsService
import org.ergoplatform.explorer.utils.Paging

class TransactionsHandlerSpec extends HttpSpec {

  val infoResp = TransactionSummaryInfo("test", 0L, 1L, 2L, MiniBlockInfo("r", 5L), List.empty, List.empty)

  private val service = new TransactionsService[IO] {

    override def getTxInfo(id: String): IO[TransactionSummaryInfo] = IO.pure(infoResp)

    override def getTxsByAddressId(addressId: String, p: Paging): IO[List[TransactionInfo]] = ???

    override def countTxsByAddressId(addressId: String): IO[Long] = ???

    override def searchById(query: String): IO[List[String]] = ???

    override def getOutputsByErgoTree(ergoTree: String): IO[List[OutputInfo]] = ???

    override def getOutputsByHash(hash: String): IO[List[OutputInfo]] = ???
  }

  val route: Route = new TransactionsHandler(service).route

  it should "return tx info by id" in {
    Get("/transactions/0001111abb") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe infoResp.asJson
    }
  }

}
