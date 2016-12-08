package net.dericbourg.ratp.gtfs

case class Trip(routeId: Long, serviceId: Long, tripId: Long, tripShortName: String, directionId: Long)

object Trip {
  def parse(fields: Map[String, String]) = {
    Trip(
      fields("route_id").toLong,
      fields("service_id").toLong,
      fields("trip_id").toLong,
      fields("trip_short_name"),
      fields("direction_id").toLong
    )
  }
}
