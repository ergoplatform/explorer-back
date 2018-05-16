package org.ergoplatform.explorer

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import org.ergoplatform.explorer.config.ExplorerConfig

import scala.concurrent.ExecutionContext

trait Setup {
  implicit val system: ActorSystem
  implicit val mat: ActorMaterializer
  implicit val ec: ExecutionContext
}
