package org.ergoplatform.explorer.config

import scala.concurrent.duration.FiniteDuration

case class GrabberConfig(nodes: List[String], pollDelay: FiniteDuration)
