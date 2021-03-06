package net.dericbourg.ratp.gtfs

import java.io.{File, FilenameFilter}

import com.github.tototoshi.csv._
import net.dericbourg.db.UsingPostgres
import net.dericbourg.ratp.gtfs.model._

object Import extends App {

  val gtfsRootDirectory = new File(s"${System.getProperty("user.home")}/tmp/RATP_GTFS")
  val lineDirectories: Array[File] = gtfsRootDirectory.
    listFiles(new FilenameFilter() {
      override def accept(dir: File, name: String): Boolean = name.startsWith("RATP_GTFS_")
    }).
    filter(_.isDirectory)

  lineDirectories.foreach { directory =>
    println(s"Parsing routes from ${directory.getName}")
    CSVReader.open(new File(directory, "routes.txt"))
      .allWithHeaders()
      .map(Route.parse)
      .grouped(1000)
      .toList
      .par
      .foreach { routeBatch =>
        UsingPostgres { connection =>
          val preparedStatement = connection.prepareStatement(Query.Route)
          routeBatch.foreach { (route: Route) =>
            import route._
            preparedStatement.setLong(1, routeId)
            preparedStatement.setString(2, routeShortName)
            preparedStatement.setString(3, routeLongName)
            preparedStatement.setString(4, routeDesc)
            preparedStatement.addBatch()
          }
          preparedStatement.executeBatch()
        }
      }
  }

  lineDirectories.foreach { directory =>
    println(s"Parsing stops from ${directory.getName}")
    CSVReader.open(new File(directory, "stops.txt"))
      .allWithHeaders()
      .map(Stop.parse)
      .grouped(1000)
      .toList
      .par
      .foreach { stopBatch =>
        UsingPostgres { connection =>
          val preparedStatement = connection.prepareStatement(Query.Stop)
          stopBatch.foreach { (stop: Stop) =>
            import stop._
            val point = s"POINT($stopLat $stopLon)"
            preparedStatement.setLong(1, stopId)
            preparedStatement.setString(2, stopName)
            preparedStatement.setString(3, stopDesc)
            preparedStatement.setDouble(4, stopLat)
            preparedStatement.setDouble(5, stopLon)
            preparedStatement.setInt(6, locationType)
            preparedStatement.setString(7, point)
            preparedStatement.setString(8, parentStation)
            preparedStatement.addBatch()
          }
          preparedStatement.executeBatch()
        }
      }
  }

  lineDirectories.foreach { directory =>
    println(s"Parsing trips from ${directory.getName}")
    CSVReader.open(new File(directory, "trips.txt"))
      .allWithHeaders()
      .map(Trip.parse)
      .grouped(1000)
      .toList
      .par
      .foreach { tripBatch =>
        UsingPostgres { connection =>
          val preparedStatement = connection.prepareStatement(Query.Trip)
          tripBatch.foreach { (trip: Trip) =>
            import trip._
            preparedStatement.setLong(1, tripId)
            preparedStatement.setLong(2, routeId)
            preparedStatement.setLong(3, serviceId)
            preparedStatement.setString(4, tripShortName)
            preparedStatement.setString(5, tripShortName)
            preparedStatement.addBatch()
          }
          preparedStatement.executeBatch()
        }
      }
  }

  lineDirectories.foreach { directory =>
    println(s"Parsing stop times from ${directory.getName}")
    CSVReader.open(new File(directory, "stop_times.txt"))
      .allWithHeaders()
      .map(StopTime.parse)
      .grouped(1000)
      .toList
      .par
      .foreach { stopTimesBatch =>
        UsingPostgres { connection =>
          val preparedStatement = connection.prepareStatement(Query.StopTime)
          stopTimesBatch.foreach { (stopTime: StopTime) =>
            import stopTime._
            preparedStatement.setLong(1, tripId)
            preparedStatement.setLong(2, arrivalTime.toMillis)
            preparedStatement.setLong(3, departureTime.toMillis)
            preparedStatement.setLong(4, stopId)
            preparedStatement.addBatch()
          }
          preparedStatement.executeBatch()
        }
      }
  }

  lineDirectories.foreach { directory =>
    println(s"Parsing transfers from ${directory.getName}")
    CSVReader.open(new File(directory, "transfers.txt"))
      .allWithHeaders()
      .map(Transfer.parse)
      .grouped(1000)
      .toList
      .par
      .foreach { transfersBatch =>
        UsingPostgres { connection =>
          val preparedStatement = connection.prepareStatement(Query.Transfer)
          transfersBatch.foreach { (transfer: Transfer) =>
            import transfer._
            preparedStatement.setLong(1, fromStopId)
            preparedStatement.setLong(2, toStopId)
            preparedStatement.setString(3, transferType)
            preparedStatement.setLong(4, minTransferTime)
            preparedStatement.setLong(5, fromStopId)
            preparedStatement.setLong(6, toStopId)
            preparedStatement.addBatch()
          }
          preparedStatement.executeBatch()
        }
      }
  }

  UsingPostgres { connection =>
    Query.Refresh.foreach { refresh =>
      val preparedCall = connection.prepareCall(refresh)
      preparedCall.execute()
    }
  }
}

object Query {
  val Route: String =
    """
      |insert into route (id, short_name, long_name, description)
      |values (?, ?, ?, ?)
    """.stripMargin

  val Stop: String =
    """
      |insert into stop (id, name, description, latitude, longitude, location_type, coordinates, parent_station)
      |select ?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 4326), ?
    """.stripMargin

  val Trip: String =
    """
      |insert into trip (id, route_id, service_id, short_name, long_name)
      |values (?, ?, ?, ?, ?)
    """.stripMargin

  val StopTime: String =
    """
      |insert into stop_time (trip_id, arrival_time, departure_time, stop_id)
      |values (?, ?, ?, ?)
    """.stripMargin

  val Transfer: String =
    """
      |insert into transfer (from_stop_id, to_stop_id, transfer_type, min_transfer_time)
      |select ?, ?, ?, ?
      |where exists (select 1 from stop where id = ?)
      |and exists (select 1 from stop where id = ?);
    """.stripMargin

  val Refresh: Seq[String] = Seq(
    "refresh materialized view route_index;",
    "refresh materialized view enriched_connection;",
    "refresh materialized view enriched_transfer;",
    "refresh materialized view link;",
    "refresh materialized view route_index;",
    "refresh materialized view station_reverse;"
  )
}
