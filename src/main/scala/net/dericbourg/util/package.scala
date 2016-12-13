package net.dericbourg

import java.util.concurrent.TimeUnit

import com.google.common.base.Stopwatch

import scala.concurrent.duration.Duration

package object util {

  case class Timed[R](result: R, duration: Duration)

  private val precision: TimeUnit = TimeUnit.MILLISECONDS

  def timed[R](block: => R): Timed[R] = {
    val stopwatch = Stopwatch.createStarted()
    val result = block
    Timed(result, Duration(stopwatch.elapsed(precision), precision))
  }

  implicit def asTuple[R](timed: Timed[R]): (R, Duration) = (timed.result, timed.duration)
}
