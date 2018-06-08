package org.ergoplatform.explorer.http.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import cats.data._
import cats.implicits.catsKernelStdOrderForString
import org.scalatest.{FlatSpec, Matchers}
import scorex.crypto.encode.Base16

class CommonDirectivesSpec extends FlatSpec with Matchers with ScalatestRouteTest with CommonDirectives {

  val fieldMappings = NonEmptyMap.of("notid" -> "not_id", "id" -> "id")

  val sortingEchoRoute = (get & sorting(fieldMappings, Some("id"))) { (s, so) => complete(s"$s:$so") }

  it should "read sorting parameters correctly" in {
    Get("/") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("id:ASC")
    }

    Get("/?sortBy=noTid") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("not_id:ASC")
    }

    Get("/?sortDirection=dEsC") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("id:DESC")
    }

    Get("/?sortBy=notid&sortDirection=Desc") ~> sortingEchoRoute ~> check {
      responseAs[String] shouldBe ("not_id:DESC")
    }

    Get(s"/?sortBy=notrid") ~> sortingEchoRoute ~> check {
      rejection shouldBe CommonDirectives.malformedSortByParameter("notrid", fieldMappings.keys.toNonEmptyList)
    }

    val failingSortOrder = "description"

    Get(s"/?sortBy=notid&sortDirection=$failingSortOrder") ~> sortingEchoRoute ~> check {
      rejection shouldBe CommonDirectives.malformedSortDirectionParameter(failingSortOrder)
    }

    Get(s"/?sortBy=notrid&sortDirection=$failingSortOrder") ~> sortingEchoRoute ~> check {
      rejections should contain allOf(
        CommonDirectives.malformedSortDirectionParameter(failingSortOrder),
        CommonDirectives.malformedSortByParameter("notrid", fieldMappings.keys.toNonEmptyList)
      )
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

  val base16EchoRoute = (get & base16Segment) { s => complete(s) }

  it should "read base16 strings from path correctly" in {

    val correctBase58 = Base16.Alphabet
    val failure1 = correctBase58 + "GJHGAJAHDF!"

    Get("/" + correctBase58) ~> base16EchoRoute ~> check {
      responseAs[String] shouldBe Base16.Alphabet
    }

    Get("/" + failure1) ~> base16EchoRoute ~> check {
      rejection shouldBe CommonDirectives.base16ValidationError
    }
  }

  val durationEcho = (get & duration) { d => complete(d.toString)}

  it should "read timespan correctly" in {
    Get("/") ~> durationEcho ~> check {
      responseAs[String] shouldBe "-1"
    }

    Get("/?timespan=all") ~> durationEcho ~> check {
      responseAs[String] shouldBe "-1"
    }

    Get("/?timespan=1DaY") ~> durationEcho ~> check {
      responseAs[String] shouldBe "1"
    }

    Get("/?timespan=7DaYs") ~> durationEcho ~> check {
      responseAs[String] shouldBe "7"
    }

    Get("/?timespan=30days") ~> durationEcho ~> check {
      responseAs[String] shouldBe "30"
    }

    Get("/?timespan=60days") ~> durationEcho ~> check {
      responseAs[String] shouldBe "60"
    }

    Get("/?timespan=180days") ~> durationEcho ~> check {
      responseAs[String] shouldBe "180"
    }

    Get("/?timespan=1year") ~> durationEcho ~> check {
      responseAs[String] shouldBe "365"
    }

    Get("/?timespan=2years") ~> durationEcho ~> check {
      responseAs[String] shouldBe "730"
    }

    Get("/?timespan=2DaYs") ~> durationEcho ~> check {
      rejection shouldBe CommonDirectives.malformedTimespanParameter
    }
  }

  val startEndEcho = (get & startEndDate) { (s, e) => complete(s"$s:$e") }

  it should "read start end date correctly" in {
    Get("/") ~> startEndEcho ~> check {
      responseAs[String].startsWith("0:") shouldBe true
    }

    Get("/?endDate=100") ~> startEndEcho ~> check {
      responseAs[String] shouldBe "0:100"
    }

    Get("/?startDate=10&endDate=100") ~> startEndEcho ~> check {
      responseAs[String] shouldBe "10:100"
    }

    Get("/?startDate=100&endDate=10") ~> startEndEcho ~> check {
      rejection shouldBe CommonDirectives.malformedStartEndDateParam
    }
  }

}
