package org.ergoplatform.explorer.http.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{FlatSpec, Matchers}

class CommonDirectivesSpec extends FlatSpec with Matchers with ScalatestRouteTest with CommonDirectives {

  val sortingEchoRoute = (get & sorting) { (s, so) => complete(s"$s:$so") }

  it should "read sorting parameters correctly" in {
    Get("/") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("id:ASC")
    }

    Get("/?sortBy=notid") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("notid:ASC")
    }

    Get("/?sortDirection=dEsC") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("id:DESC")
    }

    Get("/?sortBy=notid&sortDirection=Desc") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("notid:DESC")
    }

    val failingSortOrder = "description"

    Get(s"/?sortBy=notid&sortDirection=$failingSortOrder") ~> sortingEchoRoute ~> check {
      rejection shouldBe CommonDirectives.malformedSortDirectionParameter(failingSortOrder)
    }
  }

  val pagingEchoRoute = (get & paging) { (o, l) => complete(s"$o:$l") }

  it should "read paging parameters correctly" in {
    Get("/") ~> pagingEchoRoute ~> check {
      responseAs[String] shouldBe ("0:20")
    }

    Get("/?limit=10") ~> pagingEchoRoute ~> check {
      responseAs[String] shouldBe ("0:10")
    }

    Get("/?offset=10") ~> pagingEchoRoute ~> check {
      responseAs[String] shouldBe ("10:20")
    }

    Get("/?offset=5&limit=6") ~> pagingEchoRoute ~> check {
      responseAs[String] shouldBe ("5:6")
    }
  }

  val base58EchoRoute = (get & base58IdPath) { s => complete(s) }

  it should "read base58 strings from path correctly" in {

    val correctBase58 = "7xTVX46CWum2cUppFwfXnTFQ7ZU3FmAemcQmkL3oj5DP"
    val failure1 = "7xTVX46CWum2cUppFwfXnTFQ7ZU3FmAemcQmkL3oj5%10"
    val failure2 = "7xTVX46CWum2"
    val failure3 = "7xTVX46CWum2cUppFwfXnTFQ7ZU3FmAemcQmkL3oj5DP7xTVX46CWum2cUppFwfXnTFQ7ZU3FmAemcQmkL3oj5DP"

    Get("/" + correctBase58) ~> base58EchoRoute ~> check {
      responseAs[String] shouldBe "7xTVX46CWum2cUppFwfXnTFQ7ZU3FmAemcQmkL3oj5DP"
    }

    Get("/" + failure1) ~> base58EchoRoute ~> check {
      rejection shouldBe CommonDirectives.base58ValidationError
    }

    Get("/" + failure2) ~> base58EchoRoute ~> check {
      rejection shouldBe CommonDirectives.base58ValidationError
    }

    Get("/" + failure3) ~> base58EchoRoute ~> check {
      rejection shouldBe CommonDirectives.base58ValidationError
    }
  }

  val durationEcho = (get & duration) { d => complete(d.toString)}

  it should "read timespan correctly" in {
    Get("/") ~> durationEcho ~> check {
      responseAs[String] shouldBe "Duration.Inf"
    }

    Get("/?timespan=all") ~> durationEcho ~> check {
      responseAs[String] shouldBe "Duration.Inf"
    }

    Get("/?timespan=1DaY") ~> durationEcho ~> check {
      responseAs[String] shouldBe "1 day"
    }

    Get("/?timespan=7DaYs") ~> durationEcho ~> check {
      responseAs[String] shouldBe "7 days"
    }

    Get("/?timespan=30days") ~> durationEcho ~> check {
      responseAs[String] shouldBe "30 days"
    }

    Get("/?timespan=60days") ~> durationEcho ~> check {
      responseAs[String] shouldBe "60 days"
    }

    Get("/?timespan=180days") ~> durationEcho ~> check {
      responseAs[String] shouldBe "180 days"
    }

    Get("/?timespan=1year") ~> durationEcho ~> check {
      responseAs[String] shouldBe "365 days"
    }

    Get("/?timespan=2years") ~> durationEcho ~> check {
      responseAs[String] shouldBe "730 days"
    }

    Get("/?timespan=2DaYs") ~> durationEcho ~> check {
      rejection shouldBe CommonDirectives.malformedTimespanParameter
    }
  }

}
