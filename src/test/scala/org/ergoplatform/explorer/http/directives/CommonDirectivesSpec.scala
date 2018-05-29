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
  }

}
