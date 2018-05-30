package org.ergoplatform.explorer.http.directives

import akka.http.scaladsl.server._
import akka.http.scaladsl.server.Directives._
import org.ergoplatform.explorer.utils.SortOrder
import scorex.crypto.encode.Base58

import scala.concurrent.duration._
import scala.util.Success

trait CommonDirectives {

  import CommonDirectives._

  def isBase58IdStringLengthCorrect(s: String): Boolean = {
    val l = s.length
    l >= Base58IdStringMinLength && l <= Base58IdStringMaxLength
  }

  val base58IdPath: Directive1[String] = pathPrefix(Segment).flatMap(v =>
    Base58.decode(v) match {
      case Success(_) if isBase58IdStringLengthCorrect(v) => provide(v)
      case _ => reject(base58ValidationError)
    }
  )

  val paging: Directive[(Int, Int)] = parameters(("offset".as[Int] ? 0, "limit".as[Int] ? 20))

  val sorting: Directive[(String, SortOrder)] = parameters(("sortBy" ? "id", "sortDirection" ? "asc"))
    .tflatMap { case (field: String, order: String) =>
      SortOrder.fromString(order) match {
        case Some(o) =>
          tprovide(field -> o)
        case None =>
          reject(malformedSortDirectionParameter(order))
      }
    }

  val duration: Directive1[Duration] = parameters("timespan" ? "all")
    .flatMap{ v => stringToDuration(v) match {
      case Some(d) =>
        provide(d)
      case None =>
        reject(malformedTimespanParameter)

      }
    }
}

object CommonDirectives {

  val Base58IdStringMaxLength = 44
  val Base58IdStringMinLength = 32

  val malformedTimespanParameter = MalformedQueryParamRejection(
    "timespan",
    s"This param should have one of this values 'all', '7days', '30days', '60days', '180days', '1year', '2years'",
    None
  )

  val base58ValidationError = ValidationRejection("String isn't a Base58 representation")

  def malformedSortDirectionParameter(value: String) = MalformedQueryParamRejection(
    "sortDirection",
    s"This param could be asc or desc, but got $value",
    None
  )

  def stringToDuration(s: String): Option[Duration] = s.trim.toLowerCase match {
    case "all" => Some(Duration.Inf)
    case "1day" => Some(1 day)
    case "7days" => Some(7 days)
    case "30days" => Some(30 days)
    case "60days" => Some(60 days)
    case "180days" => Some(180 days)
    case "1year" => Some(365 days)
    case "2years" => Some(730 days)
    case _ => None
  }

}
