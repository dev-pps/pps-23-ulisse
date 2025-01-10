package core

import core.Route.{Id, Path, TypeRoute}
import utils.Points

trait Route:
  def id: Id
  def typology: TypeRoute
  def railsCount: Int
  def path: Path
  def length: Double

  def has(id: Id): Boolean

object Route:
  def apply(typeRoute: TypeRoute, railsCount: Int, path: Path): Route =
    RouteImpl((typeRoute, path), railsCount)

  opaque type Id = (TypeRoute, Path)
  type Station   = (String, (Double, Double))
  type Path      = (Station, Station)

  enum TypeRoute:
    case Normal, AV

  private case class RouteImpl(id: Id, railsCount: Int) extends Route:
    override def typology: TypeRoute = id._1
    override def path: Path          = id._2

    override def length: Double =
      Points.computePointsDistance(path._1._2, path._2._2)

    override def has(id: Id): Boolean =
      (this.id._1 canEqual id._1)
        && (this.id._2._1._1 equalsIgnoreCase id._2._1._1)
        && (this.id._2._2._1 equalsIgnoreCase id._2._2._1)

    override def equals(obj: Any): Boolean =
      obj match
        case that: Route => has(that.id)
        case _           => false

    override def hashCode(): Int = id.hashCode()
