package scala.core

import scala.core.Route.{Path, TypeRoute}
import scala.utils.Points

trait Route:
  def typology: TypeRoute
  def railsCount: Int
  def path: Path
  def length: Double

object Route:
  def apply(typeRoute: TypeRoute, railsCount: Int, path: Path): Route =
    RouteImpl(typeRoute, railsCount, path)

  type Station = (String, (Double, Double))
  type Path    = (Station, Station)

  enum TypeRoute:
    case Normal, AV

  private case class RouteImpl(typology: TypeRoute, railsCount: Int, path: Path)
      extends Route:
    override def length: Double =
      Points.computePointsDistance(path._1._2, path._2._2)
