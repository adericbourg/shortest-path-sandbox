package net.dericbourg.ratp.gtfs

import java.sql.ResultSet

import net.dericbourg.db.UsingPostgres
import net.dericbourg.ratp.gtfs.graph.StopNode

import scala.collection.mutable.ListBuffer


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