package net.dericbourg.ratp

import net.dericbourg.db.UsingPostgres
import spray.json._

import scala.io.Source

object StationsImport extends App {

  case class Datasets(datasets: List[Dataset])

  case class Dataset(fields: Fields)

  case class Fields(id: Int,
                    name: String,
                    description: String,
                    department: String,
                    postCode: String,
                    latitude: Double,
                    longitude: Double)

  object StationProtocol extends DefaultJsonProtocol {
    implicit val fieldsFormat: RootJsonFormat[Fields] = jsonFormat(Fields, "stop_id", "stop_name", "stop_desc", "departement", "code_postal", "stop_lat", "stop_lon")
    implicit val datasetFormat: RootJsonFormat[Dataset] = jsonFormat(Dataset, "fields")

    implicit object datasetsJsonFormat extends RootJsonFormat[Datasets] {
      def read(value: JsValue) = Datasets(value.convertTo[List[Dataset]])

      def write(f: Datasets) = ???
    }

  }

  val data = Source.fromFile(s"${System.getProperty("user.home")}/tmp/RATP/positions-geographiques-des-stations-du-reseau-ratp.json").mkString
  val stations: List[Dataset] = {
    import StationProtocol._
    data.parseJson.convertTo[Datasets].datasets
  }

  val sqlInsert =
    """
      |insert into station (id, name, description, latitude, longitude, post_code, department)
      |values (?, ?, ?, ?, ?, ?, ?);
    """.stripMargin

  stations.grouped(100).foreach { stationBatch =>
    UsingPostgres { connection =>
      val preparedStatement = connection.prepareStatement(sqlInsert)
      stationBatch.foreach { station =>
        import station.fields._
        preparedStatement.setInt(1, id)
        preparedStatement.setString(2, name)
        preparedStatement.setString(3, description)
        preparedStatement.setDouble(4, latitude)
        preparedStatement.setDouble(5, longitude)
        preparedStatement.setString(6, postCode)
        preparedStatement.setString(7, department)
        preparedStatement.addBatch()
      }
      preparedStatement.executeBatch()
      println(s"Inserted ${stationBatch.length} stations")
    }
  }
}
