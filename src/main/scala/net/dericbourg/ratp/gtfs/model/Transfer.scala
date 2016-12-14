package net.dericbourg.ratp.gtfs.model

case class Transfer(fromStopId: Int, toStopId: Int, transferType: String, minTransferTime: Int)

object Transfer {
  def parse(fields: Map[String, String]) = {
    Transfer(
      fields("from_stop_id").toInt,
      fields("to_stop_id").toInt,
      fields("transfer_type"),
      fields("min_transfer_time").toInt
    )
  }
}
