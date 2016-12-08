package net.dericbourg.ratp.gtfs

case class Stop(stopId: Long, stopName: String, stopDesc: String, stopLat: Double, stopLon: Double, locationType: Int, parentStation: String)

object Stop {
  def parse(fields: Map[String, String]) = {
    Stop(
      fields("stop_id").toLong,
      fields("stop_name"),
      fields("stop_desc"),
      fields("stop_lat").toDouble,
      fields("stop_lon").toDouble,
      fields("location_type").toInt,
      fields("parent_station")
    )
  }
}
