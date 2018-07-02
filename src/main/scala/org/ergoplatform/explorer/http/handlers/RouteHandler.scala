package org.ergoplatform.explorer.http.handlers

import akka.http.scaladsl.server.Directives.{complete, onSuccess}
import akka.http.scaladsl.server.Route
import cats.effect.IO
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.{Encoder, Json}
import org.ergoplatform.explorer.http.directives.CommonDirectives

import scala.language.implicitConversions

trait RouteHandler extends FailFastCirceSupport with CommonDirectives {

  def route: Route

  implicit def OK[R](result: IO[R])(implicit encoder: Encoder[R]): Route = {
    onSuccess(result.unsafeToFuture())(result => complete(encoder(result)))
  }

  implicit def Empty(result: IO[_]): Route = { onSuccess(result.unsafeToFuture())( _ => complete(Json.Null)) }

}
