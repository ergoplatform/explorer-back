package org.ergoplatform.explorer.db.mappings

import cats.syntax.either._
import doobie.Meta
import io.circe.Json
import io.circe.parser.parse
import org.ergoplatform.explorer.grabber.protocol.ApiDifficulty
import org.postgresql.util.PGobject

trait JsonMeta {

  implicit val JsonMeta: Meta[Json] =
    Meta.other[PGobject]("json").xmap[Json](
      a => parse(a.getValue).leftMap[Json](e => throw e).merge,
      a => {
        val o = new PGobject
        o.setType("json")
        o.setValue(a.noSpaces)
        o
      }
    )

  implicit val MetaDifficulty: Meta[ApiDifficulty] = Meta[BigDecimal].xmap(
    x => ApiDifficulty(x.toBigInt()),
    x => BigDecimal.apply(x.value)
  )

}
