package org.ergoplatform.explorer.http.directives

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server._
import cats.data._
import org.ergoplatform.explorer.utils.SortOrder
import scorex.crypto.encode.Base16

trait CommonDirectives {

  import CommonDirectives._

  val base16Segment: Directive1[String] = pathPrefix(Segment).flatMap(v =>
    v.forall(Base16.Alphabet.toSet.contains) match {
      case true => provide(v)
      case false => reject(base16ValidationError)
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



  val duration: Directive1[Int] = parameters("timespan" ? "all")
    .flatMap{ v => stringToDaysBack(v) match {
      case Some(d) =>
        provide(d)
      case None =>
        reject(malformedTimespanParameter)

      }
    }

  def checkValue[A](rej: Rejection)(value: A, f: A => Boolean): Option[Rejection] =
    if (f(value)) { None } else { Some(rej) }

  val startEndDate: Directive[(Option[Long], Option[Long])] =
    parameters(("startDate".as[Long].?, "endDate".as[Long].?)).tflatMap { case (s, e) =>

      val check = (for {
        start <- s
        end <- e
      } yield start > end).getOrElse(false)

      if (check) {
        reject(malformedStartEndDateParam)
      } else {
        tprovide((s, e))
      }
    }

}

object CommonDirectives {

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

  val base16ValidationError = ValidationRejection("String isn't a Base16 representation")

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

  def stringToDaysBack(s: String): Option[Int] = s.trim.toLowerCase match {
    case "all" => Some(-1)
    case "1day" => Some(1)
    case "7days" => Some(7)
    case "30days" => Some(30)
    case "60days" => Some(60)
    case "180days" => Some(180)
    case "1year" => Some(365)
    case "2years" => Some(730)
    case _ => None
  }
}
