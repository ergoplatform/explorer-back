package org.ergoplatform.explorer.grabber

import cats.effect.{ContextShift, IO, Timer}
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

trait LoopedIO {

  protected val ec: ExecutionContext

  protected val logger: Logger

  protected val pause: FiniteDuration

  protected val task: IO[Unit]

  protected implicit lazy val timer: Timer[IO] = IO.timer(ec)
  protected implicit lazy val cs: ContextShift[IO] = IO.contextShift(ec)

  protected def loopIO(io: IO[Unit]): IO[Unit] =
    io.attempt
      .flatMap {
        case Right(_) => IO.unit
        case Left(f)  => IO(logger.error("An error has occurred: ", f))
      }
      .flatMap(_ => IO.sleep(pause))
      .flatMap(_ => IO.suspend(loopIO(io)))

  def start: IO[Unit] = loopIO(task)

}
