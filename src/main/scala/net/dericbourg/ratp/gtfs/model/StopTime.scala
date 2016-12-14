package net.dericbourg.ratp.gtfs.model

import java.time.Duration

/**
  * For times, beginning of days are "00:mm:ss" and end of days "24:mm:ss". So we use a Duration: time since midnight.
  */
case class StopTime(tripId: Long, arrivalTime: Duration, departureTime: Duration, stopId: Long)

object StopTime {

  def parse(fields: Map[String, String]): StopTime = {
    StopTime(
      fields("trip_id").toLong,
      parseTime(fields("arrival_time")),
      parseTime(fields("departure_time")),
      fields("stop_id").toLong
    )
  }

  private def parseTime(time: String): Duration = {
    val splitTime: Array[String] = time.split(":")
    val hourOfDay: Int = splitTime(0).toInt
    val minuteOfDay: Int = splitTime(1).toInt
    val secondOfDay: Int = splitTime(2).toInt
    Duration.ofHours(hourOfDay).plusMinutes(minuteOfDay).plusSeconds(secondOfDay)
  }
}
