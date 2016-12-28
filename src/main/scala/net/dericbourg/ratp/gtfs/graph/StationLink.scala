package net.dericbourg.ratp.gtfs.graph

import net.dericbourg.ratp.gtfs.FWQuery.LinkType.LinkType
import org.apache.commons.graph.Mapper

class StationLink(val weight: Int, val head: StopNode, val tail: StopNode, val linkType: LinkType) {
  override def toString = s"StationLink(weight=$weight, head=$head, tail=$tail, linkType=$linkType)"
}

class StationLinkMapper extends Mapper[StationLink, Int] {
  override def map(input: StationLink): Int = input.weight
}