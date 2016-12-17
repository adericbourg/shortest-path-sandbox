package net.dericbourg.ratp.gtfs.graph

import org.apache.commons.graph.weight.OrderedMonoid

class Monoid extends OrderedMonoid[Int] {
  override def compare(o1: Int, o2: Int): Int = o1.compareTo(o2)

  override def identity(): Int = 0

  override def append(e1: Int, e2: Int): Int = e1 + e2

  override def inverse(element: Int): Int = -element
}
