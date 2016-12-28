package net.dericbourg.ratp.gtfs

import java.sql.ResultSet

import net.dericbourg.db.UsingPostgres
import net.dericbourg.ratp.gtfs.FWQuery.LinkType.LinkType
import net.dericbourg.ratp.gtfs.graph.StopNode

import scala.collection.mutable.ListBuffer


object FWQuery {

  case class Link(from: Long, to: Long, duration: Int, linkType: LinkType)

  object LinkType {

    sealed trait LinkType

    case object Line extends LinkType

    case object Transfer extends LinkType

    case object Self extends LinkType

    case object Unknown extends LinkType

    def apply(source: String): LinkType = {
      source match {
        case "connection" => Line
        case "transfer" => Transfer
        case "self" => Self
        case _ => Unknown
      }
    }
  }


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
    val statement = connection.prepareStatement("select start_stop_id, arrival_stop_id, connection_duration, source from link")
    val resultSet: ResultSet = statement.executeQuery()
    val buffer = new ListBuffer[Link]
    while (resultSet.next()) {
      val link = Link(
        resultSet.getLong(1),
        resultSet.getLong(2),
        resultSet.getInt(3),
        LinkType(resultSet.getString(4))
      )
      buffer += link
    }
    buffer.toList
  }

  def storeDistances(weights: Iterable[StationToStationWeight]) = UsingPostgres { connection =>
    val statement = connection.prepareStatement("insert into station_distance (source, target, weight, amortized_weight, trip) values (?, ?, ?, ?, ?)")
    weights.foreach { weight =>
      statement.setLong(1, weight.source.id)
      statement.setLong(2, weight.target.id)
      statement.setInt(3, weight.weight)
      statement.setInt(4, weight.amortizedWeight)
      statement.setString(5, weight.trip.toString())
      statement.addBatch()
    }
    statement.executeBatch()
  }
}