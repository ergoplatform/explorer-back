package org.ergoplatform.explorer

import org.ergoplatform.explorer.http.handlers.HelloWorldHandler

trait Rest { self: Setup =>
  val routes = new HelloWorldHandler().route
}
