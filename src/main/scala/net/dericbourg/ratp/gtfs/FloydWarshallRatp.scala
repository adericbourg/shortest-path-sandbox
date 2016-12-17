package net.dericbourg.ratp.gtfs

import java.sql.ResultSet

import net.dericbourg.db.UsingPostgres
import net.dericbourg.util._
import org.apache.commons.graph.Mapper
import org.apache.commons.graph.model.DirectedMutableGraph
import org.apache.commons.graph.shortestpath.{AllVertexPairsShortestPath, DefaultWeightedEdgesSelector}
import org.apache.commons.graph.weight.OrderedMonoid

import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer
import scala.concurrent.duration.Duration
import scala.util.Try

object FloydWarshallRatp extends App {

  val (nodes, fetchNodesDuration): (Map[Long, StopNode], Duration) = timed {
    FWQuery.stops
      .groupBy(_.id)
      .mapValues(_.head)
  }.asTuple()
  val (links, fetchLinksDuration): (Seq[StationLink], Duration) = timed {
    FWQuery.links
      .map(link => new StationLink(link.duration, nodes(link.from), nodes(link.to)))
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

object FWQuery {

  case class Link(from: Long, to: Long, duration: Int)

  def stops: Seq[StopNode] = UsingPostgres { connection =>
    val statement = connection.prepareStatement("select id, name from stop")
    val resultSet: ResultSet = statement.executeQuery()
    val buffer = new ListBuffer[StopNode]
    while (resultSet.next()) {
      val node = StopNode(
        resultSet.getLong(1),
        resultSet.getString(2)
      )
      buffer += node
    }
    buffer.toList
  }

  def links: Seq[Link] = UsingPostgres { connection =>
    val statement = connection.prepareStatement("select start_stop_id, arrival_stop_id, connection_duration from all_links")
    val resultSet: ResultSet = statement.executeQuery()
    val buffer = new ListBuffer[Link]
    while (resultSet.next()) {
      val link = Link(
        resultSet.getLong(1),
        resultSet.getLong(2),
        resultSet.getInt(3)
      )
      buffer += link
    }
    buffer.toList
  }
}

case class StopNode(id: Long, name: String)

class StationLink(val weight: Int, val head: StopNode, val tail: StopNode) {
  override def toString = s"StationLink(weight=$weight, head=$head, tail=$tail)"
}

class StationLinkMapper extends Mapper[StationLink, Int] {
  override def map(input: StationLink): Int = input.weight
}

class Monoid extends OrderedMonoid[Int] {
  override def compare(o1: Int, o2: Int): Int = o1.compareTo(o2)

  override def identity(): Int = 0

  override def append(e1: Int, e2: Int): Int = e1 + e2

  override def inverse(element: Int): Int = -element
}

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

class RatpGraph {

  import scala.collection.JavaConverters._

  val underlying = new DirectedMutableGraph[StopNode, StationLink]()

  def addVertex(vertex: StopNode): Unit = underlying.addVertex(vertex)

  def vertices: Seq[StopNode] = underlying.getVertices.asScala.toSeq

  def addEdge(edge: StationLink): Unit = {
    // For now, there are some duplicates in the results
    Try(underlying.addEdge(edge.head, edge, edge.tail))
  }
}
