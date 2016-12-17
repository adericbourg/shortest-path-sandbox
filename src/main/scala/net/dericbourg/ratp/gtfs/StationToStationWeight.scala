package net.dericbourg.ratp.gtfs

import net.dericbourg.ratp.gtfs.graph.StopNode

case class StationToStationWeight(source: StopNode, target: StopNode, weight: Int)
