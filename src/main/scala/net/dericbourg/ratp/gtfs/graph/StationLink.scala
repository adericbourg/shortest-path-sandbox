package net.dericbourg.ratp.gtfs.graph

import org.apache.commons.graph.Mapper

class StationLink(val weight: Int, val head: StopNode, val tail: StopNode) {
  override def toString = s"StationLink(weight=$weight, head=$head, tail=$tail)"
}

class StationLinkMapper extends Mapper[StationLink, Int] {
  override def map(input: StationLink): Int = input.weight
}