package org.ergoplatform.explorer.grabber

import java.io.InputStream

import cats.MonadError
import cats.syntax.all._
import io.circe.{Decoder, DecodingFailure, Json, ParsingFailure}
import scalaj.http.{Http, HttpRequest}

import scala.io.Source


trait RequestService[F[_]] {

  def get[T](uri: String)(implicit e: Decoder[T]): F[T]
}

class RequestServiceImpl[F[_]](implicit F: MonadError[F, Throwable]) extends RequestService[F] {

  type RequestParser[T] = (Int, Map[String, IndexedSeq[String]], InputStream) => T

  override def get[T](uri: String)(implicit d: Decoder[T]): F[T] = for {
    r <- makeGetRequest(uri)
    json <- executeRequest(r)
    entity <- decode(json, d)
  } yield entity

  private def inputStreamToJson(is: InputStream): F[Json] = {
    val str = Source.fromInputStream(is, "UTF8").mkString
    io.circe.parser.parse(str) match {
      case Right(json) => F.pure(json)
      case Left(pf: ParsingFailure) => F.raiseError[Json](pf.underlying)
    }
  }

  private def decode[T](json: Json, d: Decoder[T]): F[T] = {
    d.decodeJson(json) match {
      case Right(v) =>
        F.pure(v)
      case Left(df: DecodingFailure) =>
        F.raiseError(new IllegalArgumentException(s"Cannot decode entity from json, failure: ${df.message}"))
    }
  }

  private def executeRequest(r: HttpRequest): F[Json] = {
    val requestParser: RequestParser[F[Json]] = (code, _, is) => code match {
      case 200 =>
        inputStreamToJson(is)
      case _ =>
        val msg = Source.fromInputStream(is, "UTF8").mkString
        F.raiseError(
          new IllegalStateException(s"Request to ${r.url} has been failed with code ${code}, and message $msg")
        )
    }

    r.exec(requestParser).body
  }

  private def makeGetRequest(uri: String): F[HttpRequest] = F.pure(Http(uri))
}
