package org.ergoplatform.explorer.grabber.http

import java.io.InputStream

import cats.MonadError
import cats.syntax.flatMap._
import cats.effect.{IO, LiftIO}
import com.typesafe.scalalogging.Logger
import io.circe.{Decoder, DecodingFailure, Json, ParsingFailure}
import scalaj.http.{Http, HttpRequest}

import scala.io.Source

trait RequestService[F[_]] {

  def get[T](uri: String)(implicit d: Decoder[T]): F[T]

  def getSafe[T](uri: String)(implicit d: Decoder[T]): F[Either[Throwable, T]]

}

class RequestServiceImpl[F[_]](implicit F: MonadError[F, Throwable], l: LiftIO[F])
  extends RequestService[F] {

  private val logger = Logger("request-service")

  type RequestParser[T] = (Int, Map[String, IndexedSeq[String]], InputStream) => T

  private def getIO[T](uri: String)(implicit d: Decoder[T]): IO[T] =
    for {
      r      <- makeRequest(uri)
      json   <- executeRequest(r)
      entity <- decode(json, d)
    } yield entity

  override def get[T](uri: String)(implicit d: Decoder[T]): F[T] = l.liftIO(getIO[T](uri))

  override def getSafe[T](uri: String)(implicit d: Decoder[T]): F[Either[Throwable, T]] =
    l.liftIO(getIO[T](uri).attempt)

  private def inputStreamToJson(is: InputStream): IO[Json] = {
    val str = Source.fromInputStream(is, "UTF8").mkString
    io.circe.parser.parse(str) match {
      case Right(json)              => IO.pure(json)
      case Left(pf: ParsingFailure) => IO.raiseError[Json](pf.underlying)
    }
  }

  private def decode[T](json: Json, d: Decoder[T]): IO[T] = {
    d.decodeJson(json) match {
      case Right(v) =>
        IO.pure(v)
      case Left(df: DecodingFailure) =>
        IO.raiseError(
          new IllegalArgumentException(s"Cannot decode entity from json, failure: ${df.message}")
        )
    }
  }

  private def executeRequest(r: HttpRequest): IO[Json] = {
    val requestParser: RequestParser[IO[Json]] = (code, _, is) =>
      code match {
        case 200 =>
          inputStreamToJson(is)
        case _ =>
          val msg = Source.fromInputStream(is, "UTF8").mkString
          IO.delay(logger.error(s"Request to ${r.url} has been failed with code $code, and message $msg")) >>
          IO.raiseError(
            new IllegalStateException(
              s"Request to ${r.url} has been failed with code $code, and message $msg"
            )
          )
    }

    r.exec(requestParser).body.handleErrorWith { e =>
      IO.delay(logger.error(s"Request to ${r.url} has been failed. ${e.getMessage}")) >>
      IO.raiseError(e)
    }
  }

  private def makeRequest(uri: String): IO[HttpRequest] = IO.pure(Http(uri))

}
