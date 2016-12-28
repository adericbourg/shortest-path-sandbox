package net.dericbourg.ratp.gtfs.graph

import net.dericbourg.ratp.gtfs.FWQuery.LinkType._
import org.apache.commons.graph.Mapper

class StationLink(val weight: Int, val head: StopNode, val tail: StopNode, val linkType: LinkType) {
  override def toString = s"StationLink(weight=$weight, head=$head, tail=$tail, linkType=$linkType)"
}

object StationLink {

  import play.api.libs.functional.syntax._
  import play.api.libs.json._

  implicit val StopNodeWrites: Writes[StopNode] = (
    (JsPath \ "id").write[Long] and
      (JsPath \ "name").write[String]
    ) (unlift(StopNode.unapply))

  implicit object LinkTypeWrites extends Writes[LinkType] {
    def writes(linkType: LinkType): JsValue = linkType match {
      case Self => Json.toJson("self")
      case Transfer => Json.toJson("transfer")
      case Line => Json.toJson("connection")
      case _ => Json.toJson("unknown")
    }
  }

  implicit val StationLinkWrites: Writes[StationLink] = (
    (JsPath \ "weight").write[Int] and
      (JsPath \ "head").write[StopNode] and
      (JsPath \ "tail").write[StopNode] and
      (JsPath \ "link_type").write[LinkType]
    ) (unlift(StationLink.unapply))

  def unapply(arg: StationLink): Option[(Int, StopNode, StopNode, LinkType)] = Some((arg.weight, arg.head, arg.tail, arg.linkType))
}

class StationLinkMapper extends Mapper[StationLink, Int] {
  override def map(input: StationLink): Int = input.weight
}