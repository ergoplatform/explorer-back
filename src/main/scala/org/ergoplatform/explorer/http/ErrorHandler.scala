package org.ergoplatform.explorer.http

import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server._
import com.typesafe.scalalogging.StrictLogging
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.Json
import org.ergoplatform.explorer.http.protocol.ApiError

trait ErrorHandler extends FailFastCirceSupport with StrictLogging {

  implicit val exceptionHandler: ExceptionHandler = ExceptionHandler {
    case e: ApiError                 => error(e, StatusCodes.custom(e.statusCode, e.msg))
    case e: NoSuchElementException   => error(e, StatusCodes.NotFound)
    case e: IllegalArgumentException => error(e, StatusCodes.BadRequest)
    case e: Throwable                => error(e, StatusCodes.InternalServerError)
  }

  private def error(e: Throwable, code: StatusCode): Route = {
    logger.info("Error processing request. " + e, e)
    complete(code -> Json.obj("msg" -> Json.fromString(e.getMessage)))
  }

  implicit val rejectionHandler: RejectionHandler = RejectionHandler
    .newBuilder()
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
    "msg"    -> Json.fromString("Param is malformed"),
    "param"  -> Json.fromString(paramName),
    "reason" -> Json.fromString(error)
  )
}
