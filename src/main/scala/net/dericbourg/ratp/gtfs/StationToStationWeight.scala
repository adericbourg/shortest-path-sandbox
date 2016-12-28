package net.dericbourg.ratp.gtfs

import net.dericbourg.ratp.gtfs.graph.StopNode
import play.api.libs.json.JsValue

case class StationToStationWeight(source: StopNode, target: StopNode, weight: Int, amortizedWeight: Int, trip: JsValue)
