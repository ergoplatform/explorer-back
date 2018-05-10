package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json

class HelloWorldHandler extends FailFastCirceSupport {

  val route = get {
    pathPrefix("hello") {
      complete(Json.obj("result" -> Json.fromString("world")))
    } ~ pathPrefix("error") {
      complete(new IllegalArgumentException("Error test!"))
    }
  }

}
