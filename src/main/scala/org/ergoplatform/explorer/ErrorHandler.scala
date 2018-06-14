package org.ergoplatform.explorer

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ExceptionHandler, RejectionHandler, ValidationRejection}
import akka.http.scaladsl.server._
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
    case e: Throwable =>
      complete(StatusCodes.InternalServerError -> Json.obj("msg" -> Json.fromString(e.getMessage)))
  }

  implicit val rejectionHandler = RejectionHandler.newBuilder()
    .handle {
      case ValidationRejection(reason, _) =>
        complete(StatusCodes.BadRequest -> Json.obj("msg" -> Json.fromString(reason)))
      case MalformedQueryParamRejection(p, e, _) =>
        complete(StatusCodes.BadRequest -> queryParamError(p, e))
    }
    .handleNotFound {
      complete(StatusCodes.NotFound -> Json.obj("msg" -> Json.fromString("Not Found")))
    }
    .result()


  def queryParamError(paramName: String, error: String): Json = Json.obj(
    "msg" -> Json.fromString("Param is malformed"),
    "param" -> Json.fromString(paramName),
    "reason" -> Json.fromString(error)
  )
}
