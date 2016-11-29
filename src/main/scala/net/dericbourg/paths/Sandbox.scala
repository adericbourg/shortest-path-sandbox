package net.dericbourg.paths

import org.apache.commons.graph.Mapper
import org.apache.commons.graph.model.UndirectedMutableGraph
import org.apache.commons.graph.shortestpath.DefaultWeightedEdgesSelector
import org.apache.commons.graph.weight.OrderedMonoid

object Sandbox extends App {

  val graph = new SandboxGraph

  val a = Vertex("a")
  val b = Vertex("b")
  val c = Vertex("c")
  val d = Vertex("d")
  val e = Vertex("e")
  val f = Vertex("f")
  val g = Vertex("g")
  val h = Vertex("h")
  val i = Vertex("i")


  graph.addVertex(a)
  graph.addVertex(b)
  graph.addVertex(c)
  graph.addVertex(d)
  graph.addVertex(e)
  graph.addVertex(f)
  graph.addVertex(g)
  graph.addVertex(h)
  graph.addVertex(i)

  graph.addEdge(new WeightedEdge(1, a, b))
  graph.addEdge(new WeightedEdge(1, a, f))
  graph.addEdge(new WeightedEdge(1, b, f))
  graph.addEdge(new WeightedEdge(1, b, i))
  graph.addEdge(new WeightedEdge(1, c, h))
  graph.addEdge(new WeightedEdge(1, d, h))
  graph.addEdge(new WeightedEdge(1, e, h))
  graph.addEdge(new WeightedEdge(1, f, g))
  graph.addEdge(new WeightedEdge(1, g, h))
  graph.addEdge(new WeightedEdge(1, g, i))

  class WeightedEdgeMapper extends Mapper[WeightedEdge, Int] {
    override def map(input: WeightedEdge): Int = input.weight
  }

  val pathSourceSelector = new DefaultWeightedEdgesSelector(graph.underlying)
    .whereEdgesHaveWeights[Int, WeightedEdgeMapper](new WeightedEdgeMapper)

  class Monoid extends OrderedMonoid[Int] {
    override def compare(o1: Int, o2: Int): Int = o1.compareTo(o2)

    override def identity(): Int = 0

    override def append(e1: Int, e2: Int): Int = e1 + e2

    override def inverse(element: Int): Int = -element
  }

  val floydWarshall = pathSourceSelector.applyingFloydWarshall(new Monoid)
  val shortestPathAE = floydWarshall.findShortestPath(a, e)
  shortestPathAE.getEdges.forEach(println)
}

case class Vertex(name: String)

class WeightedEdge(val weight: Int, val head: Vertex, val tail: Vertex) {

  override def toString = s"WeightedEdge(weight=$weight, head=$head, tail=$tail)"
}

class SandboxGraph {

  val underlying = new UndirectedMutableGraph[Vertex, WeightedEdge]()

  def addVertex(vertex: Vertex): Unit = underlying.addVertex(vertex)

  def addEdge(edge: WeightedEdge): Unit = underlying.addEdge(edge.head, edge, edge.tail)
}
