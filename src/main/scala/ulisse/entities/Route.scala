package ulisse.entities

import Coordinates.Geo
import Route.{Id, Path, TypeRoute}

trait Route(val id: Id, val length: Double, val railsCount: Int):
  def typology: TypeRoute
  def path: Path

  def setTopology(typeRoute: TypeRoute): Route
  def setLength(length: Double): Route

  def has(id: Id): Boolean

object Route:
  def apply(typeRoute: TypeRoute, path: Path, length: Double, railsCount: Int): Route =
    RouteImpl((typeRoute, path), length, railsCount)

  opaque type Id = (TypeRoute, Path)
  type Station   = (String, ?)
  type Path      = (Station, Station)

  enum TypeRoute:
    case Normal, AV

  private case class RouteImpl(override val id: Id, override val length: Double, override val railsCount: Int)
      extends Route(id, length, railsCount):
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

    override def setTopology(typeRoute: TypeRoute): Route = Route(typeRoute, id._2, length, railsCount)

    override def setLength(length: Double): Route = Route(id._1, id._2, length, railsCount)

    override def hashCode(): Int = id.hashCode()
