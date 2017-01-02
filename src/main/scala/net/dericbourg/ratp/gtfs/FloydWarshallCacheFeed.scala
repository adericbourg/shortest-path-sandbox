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
          val strippedPath = getStrippedTrip(shortestPath.getEdges)
          val amortizedWeight = getTotalWeight(strippedPath)
          val trip = Json.toJson(strippedPath)
          (shortestPath.getWeight, amortizedWeight, trip)
        }

      StationToStationWeight(startNode, endNode, weight, amortizedWeight, trip)
    }
    println(s"Inserting distances from station $startNode")
    FWQuery.storeDistances(weights)
  }

  private def getStrippedTrip(edges: java.lang.Iterable[StationLink]): Seq[StationLink] = {
    getStrippedTrip(edges.asScala.toList)
  }

  private def getStrippedTrip(edges: Seq[StationLink]): Seq[StationLink] = {
    val strippedStart = startFromConnection(edges)
    val strippedReverse = startFromConnection(strippedStart.reverse)
    strippedReverse.reverse
  }

  @tailrec
  private def startFromConnection(edges: Seq[StationLink]): Seq[StationLink] = {
    edges match {
      case head :: Nil if head.linkType == Self || head.linkType == Transfer =>
        val amortized = new StationLink(
          weight = 0,
          head = head.head,
          tail = head.tail,
          linkType = head.linkType
        )
        Seq(amortized)
      case head :: tail if head.linkType == Self || head.linkType == Transfer =>
        startFromConnection(tail)
      case _ => edges
    }
  }

  private def getTotalWeight(edges: Iterable[StationLink]): Int = edges.map(_.weight).sum

}