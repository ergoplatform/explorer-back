package org.ergoplatform.explorer.utils

trait SortOrder

object SortOrder {
  def fromString(v: String): Option[SortOrder] = v.trim.toLowerCase match {
    case "asc" => Some(Asc)
    case "desc" => Some(Desc)
    case _ => None
  }
}

case object Asc extends SortOrder {
  override def toString: String = "ASC"
}

case object Desc extends SortOrder {
  override def toString: String = "DESC"
}

