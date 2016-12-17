package net.dericbourg.ratp.gtfs

import net.dericbourg.ratp.gtfs.graph.StopNode


class Stats(vertex: StopNode, values: Seq[Int]) {


  val standardDeviation: Double = Stats.standardDeviation(values)
  val mean: Double = Stats.mean(values)
  val sum: Int = values.sum


  override def toString = s"Stats(${vertex.name} (${vertex.id}): (mean: $mean, stdDev: $standardDeviation, sum: $sum))"
}

object Stats {

  import Numeric.Implicits._

  def apply(vertex: StopNode, values: Seq[Int]): Stats = new Stats(vertex, values)

  private[Stats] def mean[T: Numeric](xs: Iterable[T]): Double = xs.sum.toDouble / xs.size

  private[Stats] def variance[T: Numeric](xs: Iterable[T]): Double = {
    val avg = mean(xs)
    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
  }

  private[Stats] def standardDeviation[T: Numeric](xs: Iterable[T]): Double = {
    val avg = mean(xs)
    xs.map(_.toDouble).map(a => math.pow(a - avg, 2)).sum / xs.size
    math.sqrt(variance(xs))
  }
}