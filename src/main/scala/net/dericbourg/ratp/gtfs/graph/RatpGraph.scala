package net.dericbourg.ratp.gtfs.graph

import org.apache.commons.graph.model.DirectedMutableGraph

import scala.util.Try

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
