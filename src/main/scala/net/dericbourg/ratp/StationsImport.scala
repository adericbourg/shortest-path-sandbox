package net.dericbourg.ratp

import net.dericbourg.db._
import spray.json._

import scala.io.Source

object StationsImport extends App {

  case class Datasets(datasets: List[Dataset])

  case class Dataset(fields: Fields)

  case class Fields(id: Int,
                    name: String,
                    description: String,
                    department: Option[String],
                    postCode: String,
                    latitude: Double,
                    longitude: Double)

  object StationProtocol extends DefaultJsonProtocol {
    implicit val fieldsFormat: RootJsonFormat[Fields] = jsonFormat(Fields, "stop_id", "stop_name", "stop_desc", "department", "code_postal", "stop_lat", "stop_lon")
    implicit val datasetFormat: RootJsonFormat[Dataset] = jsonFormat(Dataset, "fields")

    implicit object datasetsJsonFormat extends RootJsonFormat[Datasets] {
      def read(value: JsValue) = Datasets(value.convertTo[List[Dataset]])

      def write(f: Datasets) = ???
    }
  }

  val data = Source.fromFile("/home/adericbourg/tmp/RATP/positions-geographiques-des-stations-du-reseau-ratp.json").mkString
  val stations: List[Dataset] = {
    import StationProtocol._
    data.parseJson.convertTo[Datasets].datasets
  }

  UsingPostgres { connection =>
    val query = connection.prepareStatement("SELECT Version()").executeQuery()
    query.next()
    println(query.getString(1))
  }
}
