package org.ergoplatform.explorer.http.directives

import cats.data._
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


  def checkField(mappings: NonEmptyMap[String, String])(fieldName: String): Boolean =
    mappings.lookup(fieldName.trim.toLowerCase).nonEmpty

  def sorting(fieldMappings: NonEmptyMap[String, String],
              defaultSortBy: Option[String] = None): Directive[(String, SortOrder)] = {

    val defaultSortField = defaultSortBy.getOrElse(fieldMappings.head._1)
    val checker = checkField(fieldMappings) _

    parameters(("sortBy" ? defaultSortField, "sortDirection" ? "asc"))
      .tflatMap { case (field: String, order: String) =>
        val sanitizedField = field.trim.toLowerCase
        val sortOrder = SortOrder.fromString(order)

        val sortByRej = malformedSortByParameter(field, fieldMappings.keys.toNonEmptyList)
        val sortDirRej = malformedSortDirectionParameter(order)

        val rejections = checkValue[Option[SortOrder]](sortDirRej)(sortOrder, _.nonEmpty).toList ++
          checkValue[String](sortByRej)(sanitizedField, checker).toList

        if (rejections.nonEmpty) {
          reject(rejections: _*)
        } else {
          tprovide(fieldMappings(sanitizedField).get -> sortOrder.get)
        }
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

  def checkValue[A](rej: Rejection)(value: A, f: A => Boolean): Option[Rejection] =
    if (f(value)) { None } else { Some(rej) }

  val startEndDate: Directive[(Long, Long)] =
    parameters(("startDate".as[Long] ? 0L, "endDate".as[Long] ? System.currentTimeMillis())).tflatMap { case (s, e) =>
      if (s > e) {
        reject(malformedStartEndDateParam)
      } else {
        tprovide((s, e))
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

  val malformedStartEndDateParam = MalformedQueryParamRejection(
    "startDate",
    s"Start Date can't be greater than End Date",
    None
  )

  val base58ValidationError = ValidationRejection("String isn't a Base58 representation")

  def malformedSortDirectionParameter(value: String) = MalformedQueryParamRejection(
    "sortDirection",
    s"This param could be asc or desc, but got $value",
    None
  )

  def malformedSortByParameter(value: String, availableValues: NonEmptyList[String]) = MalformedQueryParamRejection(
    "sortBy",
    s"This param could be one of ${availableValues.toList.mkString(", ")}, but got $value",
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
