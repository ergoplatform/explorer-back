package org.ergoplatform.explorer.grabber.models

import cats.syntax.either._
import io.circe.Decoder

case class NodeDifficulty(value: BigInt)

object NodeDifficulty {

  implicit val decoder: Decoder[NodeDifficulty] = Decoder.decodeString.emap { str =>
    Either
      .catchNonFatal {
        val bInt = BigInt(str)
        NodeDifficulty(bInt)
      }
      .leftMap(_ => "Difficulty")
  }
}
