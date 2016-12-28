package net.dericbourg.ratp.gtfs

import net.dericbourg.ratp.gtfs.graph._
import net.dericbourg.util._
import org.apache.commons.graph.shortestpath.{AllVertexPairsShortestPath, DefaultWeightedEdgesSelector}

import scala.collection.JavaConverters._
import scala.concurrent.duration.Duration

object FloydWarshallRatp extends App {

  val (nodes, fetchNodesDuration): (Map[Long, StopNode], Duration) = timed {
    FWQuery.stops
      .groupBy(_.id)
      .mapValues(_.head)
  }.asTuple()
  val (links, fetchLinksDuration): (Seq[StationLink], Duration) = timed {
    FWQuery.links
      .map(link => new StationLink(link.duration, nodes(link.from), nodes(link.to), link.linkType))
  }.asTuple()

  println(s"${nodes.keys.size} stations (fetched in $fetchNodesDuration)")
  println(s"${links.size} links (fetched in $fetchLinksDuration)")

  val (graph, graphBuildDuration): (RatpGraph, Duration) = timed {
    val g = new RatpGraph
    nodes.values.foreach(g.addVertex)
    links.foreach(g.addEdge)

    g
  }.asTuple()
  println(s"Graph built in $graphBuildDuration")


  val (floydWarshall, floydWarshallDuration): (AllVertexPairsShortestPath[StopNode, StationLink, Int], Duration) = timed {
    val pathSourceSelector = new DefaultWeightedEdgesSelector(graph.underlying)
      .whereEdgesHaveWeights[Int, StationLinkMapper](new StationLinkMapper)
    pathSourceSelector.applyingFloydWarshall(new Monoid)
  }.asTuple()
  println(s"Floyd-Warshall algorithm applied in $floydWarshallDuration")

  val voltaire = 1633
  val richelieuDrouot = 2447
  val buzenval = 2133
  val trinite = 1686
  val chatelet = 1964
  val stalingrad = 2485
  val stAugustin = 1715
  val stGeorges = 1720
  val ssd = 1678
  findEccentricity(Seq(
    nodes(voltaire),
    nodes(richelieuDrouot),
    nodes(buzenval)
  ))
  findEccentricity(Seq(
    nodes(trinite),
    nodes(ssd),
    nodes(stalingrad),
    nodes(chatelet),
    nodes(stGeorges)
  ))

  // Test fucked up cases on weird lines
  val javel = 1903
  val michelAngeMolitor = 1817
  val path = floydWarshall.findShortestPath(nodes(javel), nodes(michelAngeMolitor))
  path.getEdges.asScala.foreach(println)

  def findEccentricity(sources: Seq[StopNode]): Unit = {
    println("------------------")
    println(s"  Eccentricity for ${sources.mkString(", ")}")
    println("------------------")
    val (stats, duration): (Seq[Stats], Duration) = timed {
      graph.vertices
        .map { vertex =>
          val weightsPerTarget = sources
            .map { sourceVertex =>
              if (sourceVertex == vertex) 0
              else floydWarshall.findShortestPath(sourceVertex, vertex).getWeight
            }
          (vertex, weightsPerTarget)
        }
        .map { case (vertex, weights) => Stats(vertex, weights) }
        // Sorting be mean first gives more power to close vertices. Sorting by standard deviation gives more equity.
        // Mean first is more pragmatic.
        .sortBy(s => (s.mean, s.standardDeviation))
    }.asTuple()

    println(s"Optimal solution (found in $duration):")
    val optimalTarget = stats.head
    println(optimalTarget)
    println()
  }
}
