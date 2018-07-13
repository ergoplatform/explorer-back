package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import org.scalatest.{FlatSpec, Matchers}

trait HttpSpec extends FlatSpec with Matchers with ScalatestRouteTest with FailFastCirceSupport
