package org.ergoplatform.explorer.http.directives

import akka.http.scaladsl.server.{Directive, Directive1, ValidationRejection}
import akka.http.scaladsl.server.Directives._
import scorex.crypto.encode.Base58

import scala.util.Success

trait CommonDirectives {

  val Base58IdStringMaxLength = 44
  val Base58IdStringMinLength = 32

  def isBase58IdStringLengthCorrect(s: String): Boolean = {
    val l = s.length
    l >= Base58IdStringMinLength && l <= Base58IdStringMaxLength
  }

  val base58IdPath: Directive1[String] = pathPrefix(Segment).flatMap(v =>
    Base58.decode(v) match {
      case Success(_) if isBase58IdStringLengthCorrect(v) => provide(v)
      case _ => reject(ValidationRejection("String isn't a Base58 representation"))
    }
  )

  val paging: Directive[(Int, Int)] = parameters(("offset".as[Int] ? 0, "limit".as[Int] ? 20))

}
