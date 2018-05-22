package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import org.ergoplatform.explorer.utils.Converter

class UtilHandler extends FailFastCirceSupport {

  val route = pathPrefix("utils") { convert16to58String }

  val convert16to58String = (get & path("convert" / "16" / "58" / Segment)) { s =>
    complete(Json.obj("result" -> Json.fromString(Converter.from16to58(s))))
  }
}
