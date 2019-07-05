package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.model.StatusCodes
import cats.effect.IO
import io.circe.Json
import io.circe.syntax._
import org.ergoplatform.explorer.http.protocol.{BlockchainInfo, ChartSingleData, ForksSummary, MinerStatSingleInfo, StatsSummary}
import org.ergoplatform.explorer.services.StatsService

class InfoAndStatAndChartsHandlerSpec extends HttpSpec {

  val infoResp = BlockchainInfo("test", 0L, 2L, 4L)
  val statsResp = StatsSummary(0L, 1L, 2L, 3L, 4L, 5L, 6L ,7L, 2.0, 1.0, 8L, 9L ,10L)
  val forksSummaryResp = ForksSummary(0, List.empty)

  val chartsData = List(
    ChartSingleData(0L, 100L),
    ChartSingleData(1L, 200L),
    ChartSingleData(2L, 300L),
  )

  val chartsDataOther = List(
    ChartSingleData(1L, 101L),
    ChartSingleData(2L, 202L),
    ChartSingleData(3L, 303L),
  )


  val minersData = List(
    MinerStatSingleInfo("test1", 20L),
    MinerStatSingleInfo("test2", 30L)
  )

  val service = new StatsService[IO] {
    override def forksInfo(fromH: Long): IO[ForksSummary] = IO.pure(forksSummaryResp)

    override def findLastStats: IO[StatsSummary] = IO.pure(statsResp)

    override def findBlockchainInfo: IO[BlockchainInfo] = IO.pure(infoResp)

    override def totalCoinsForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsData)

    override def avgBlockSizeForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsData)

    override def totalBlockChainSizeForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsDataOther)

    override def avgDifficultyForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsData)

    override def avgTxsPerBlockForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsData)

    override def sumTxsGroupByDayForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsDataOther)

    override def minerRevenueForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsData)

    override def hashrateForDuration(daysBack: Int): IO[List[ChartSingleData[Long]]] = IO.pure(chartsData)

    override def sharesAcrossMinersFor24H: IO[List[MinerStatSingleInfo]] = IO.pure(minersData)
  }

  val route1 = new InfoHandler(service).route
  val route2 = new StatsHandler(service).route
  val route3 = new ChartsHandler(service).route

  it should "return blockchain info" in {
    Get("/info") ~> route1 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe infoResp.asJson
    }
  }

  it should "return blockchain stats" in {
    Get("/stats") ~> route2 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe statsResp.asJson
    }
  }

  it should "return charts data" in {
    Get("/charts/total") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsData.asJson
    }

    Get("/charts/block-size") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsData.asJson
    }

    Get("/charts/blockchain-size") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsDataOther.asJson
    }

    Get("/charts/transactions-per-block") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsData.asJson
    }

    Get("/charts/transactions-number") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsDataOther.asJson
    }

    Get("/charts/difficulty") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsData.asJson
    }

    Get("/charts/miners-revenue") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsData.asJson
    }

    Get("/charts/hash-rate") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe chartsData.asJson
    }

    Get("/charts/hash-rate-distribution") ~> route3 ~> check {
      status shouldBe StatusCodes.OK
      responseAs[Json] shouldBe minersData.asJson
    }
  }

}
