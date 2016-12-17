package net.dericbourg.ratp.gtfs

import net.dericbourg.ratp.gtfs.graph._
import org.apache.commons.graph.shortestpath.DefaultWeightedEdgesSelector

object FloydWarshallCacheFeed extends App {
  val nodes = FWQuery.stops
    .groupBy(_.id)
    .mapValues(_.head)

  val links = FWQuery.links
    .map(link => new StationLink(link.duration, nodes(link.from), nodes(link.to)))

  val graph = {
    val g = new RatpGraph
    nodes.values.foreach(g.addVertex)
    links.foreach(g.addEdge)

    g
  }

  val floydWarshall = {
    val pathSourceSelector = new DefaultWeightedEdgesSelector(graph.underlying)
      .whereEdgesHaveWeights[Int, StationLinkMapper](new StationLinkMapper)
    pathSourceSelector.applyingFloydWarshall(new Monoid)
  }

  nodes.values.par.foreach { startNode =>
    val weights = nodes.values.map { endNode =>
      val weight: Int =
        if (startNode == endNode) 0
        else floydWarshall.findShortestPath(startNode, endNode).getWeight
      StationToStationWeight(startNode, endNode, weight)
    }
    println(s"Inserting distances from station $startNode")
    FWQuery.storeDistances(weights)
  }
}
