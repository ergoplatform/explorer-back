package org.ergoplatform.explorer

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import org.ergoplatform.explorer.http.protocol.ApiError

object ErrorHandler extends FailFastCirceSupport {

  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: ApiError =>
      complete(StatusCodes.custom(e.statusCode, e.msg) → e)
    case e: NoSuchElementException =>
      complete(StatusCodes.NotFound -> Json.obj("msg" -> Json.fromString(e.getMessage)))
    case e: IllegalArgumentException =>
      complete(StatusCodes.BadRequest -> Json.obj("msg" -> Json.fromString(e.getMessage)))
  }
}
