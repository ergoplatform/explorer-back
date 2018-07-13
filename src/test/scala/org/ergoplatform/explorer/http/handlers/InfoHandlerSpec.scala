package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.protocol.{BlockchainInfo, ChartSingleData, MinerStatSingleInfo, StatsSummary}
import org.ergoplatform.explorer.services.StatsService

class InfoHandlerSpec extends HttpSpec {

  val infoResp = BlockchainInfo("test", 0L, 2L, 4L)

  val service = new StatsService[IO] {
    override def findLastStats: IO[StatsSummary] = ???

    override def findBlockchainInfo: IO[BlockchainInfo] = IO.pure(infoResp)

    override def totalCoinsForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def avgBlockSizeForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def totalBlockChainSizeForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def avgDifficultyForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def avgTxsPerBlockForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def minerRevenueForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def hashrateForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = ???

    override def sharesAcrossMinersFor24H: IO[List[MinerStatSingleInfo]] = ???
  }

  val route = new InfoHandler(service).route

  it should "return blockchain info" in {
    Get("/info") ~> route ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe infoResp.asJson
    }
  }

}
