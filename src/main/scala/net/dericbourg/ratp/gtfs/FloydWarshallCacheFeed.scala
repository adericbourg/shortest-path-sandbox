package net.dericbourg.ratp.gtfs

import net.dericbourg.ratp.gtfs.FWQuery.LinkType._
import net.dericbourg.ratp.gtfs.graph.StationLink._
import net.dericbourg.ratp.gtfs.graph._
import org.apache.commons.graph.shortestpath.DefaultWeightedEdgesSelector
import play.api.libs.json.{JsValue, Json}

import scala.annotation.tailrec
import scala.collection.JavaConverters._

object FloydWarshallCacheFeed extends App {
  val nodes = FWQuery.stops
    .groupBy(_.id)
    .mapValues(_.head)

  println(s"Fetched ${nodes.size} nodes")

  val links = FWQuery.links
    .map(link => new StationLink(link.duration, nodes(link.from), nodes(link.to), link.linkType))

  println(s"Fetched ${links.size} links")

  val graph = {
    val g = new RatpGraph
    nodes.values.foreach(g.addVertex)
    links.foreach(g.addEdge)

    g
  }

  println("Graph built")

  val floydWarshall = {
    val pathSourceSelector = new DefaultWeightedEdgesSelector(graph.underlying)
      .whereEdgesHaveWeights[Int, StationLinkMapper](new StationLinkMapper)
    pathSourceSelector.applyingFloydWarshall(new Monoid)
  }

  println("Floyd-Warshall computed. Saving results.")

  nodes.values.par.foreach { startNode =>
    val weights = nodes.values.map { endNode =>
      val (weight, amortizedWeight, trip): (Int, Int, JsValue) =
        if (startNode == endNode) (0, 0, Json.toJson(Seq[StationLink]()))
        else {
          val shortestPath = floydWarshall.findShortestPath(startNode, endNode)
          val trip = Json.toJson(shortestPath.getEdges.asScala.toSeq)
          (shortestPath.getWeight, getAmortizedWeight(shortestPath.getEdges), trip)
        }

      StationToStationWeight(startNode, endNode, weight, amortizedWeight, trip)
    }
    println(s"Inserting distances from station $startNode")
    FWQuery.storeDistances(weights)
  }

  private def getAmortizedWeight(edges: java.lang.Iterable[StationLink]): Int = {
    getAmortizedWeight(edges.asScala.toList)
  }

  private def getAmortizedWeight(edges: Seq[StationLink]): Int = {
    val strippedStart = stripSelfConnections(edges)
    val strippedReverse = stripSelfConnections(strippedStart.reverse)
    getTotalWeight(strippedReverse.reverse)
  }

  @tailrec
  private def stripSelfConnections(edges: Seq[StationLink]): Seq[StationLink] = {
    edges match {
      case head :: tail if head.linkType == Self => stripSelfConnections(tail)
      case _ => edges
    }
  }

  private def getTotalWeight(edges: Iterable[StationLink]): Int = edges.map(_.weight).sum
}