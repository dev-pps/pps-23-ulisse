package ulisse.entities

import Coordinate.Geo
import Route.{Id, Path, TypeRoute}

trait Route:
  def id: Id
  def typology: TypeRoute
  def railsCount: Int
  def path: Path
  def length: Double

  def has(id: Id): Boolean

object Route:
  def apply(typeRoute: TypeRoute, path: Path, length: Double, railsCount: Int): Route =
    RouteImpl((typeRoute, path), length, railsCount)

  opaque type Id = (TypeRoute, Path)
  type Station   = (String, Geo)
  type Path      = (Station, Station)

  enum TypeRoute:
    case Normal, AV

  private case class RouteImpl(id: Id, length: Double, railsCount: Int) extends Route:
    override def typology: TypeRoute = id._1
    override def path: Path          = id._2

    override def has(id: Id): Boolean =
      (this.id._1 canEqual id._1)
        && (this.id._2._1._1 equalsIgnoreCase id._2._1._1)
        && (this.id._2._2._1 equalsIgnoreCase id._2._2._1)

    override def equals(obj: Any): Boolean =
      obj match
        case that: Route => has(that.id)
        case _           => false

    override def hashCode(): Int = id.hashCode()
