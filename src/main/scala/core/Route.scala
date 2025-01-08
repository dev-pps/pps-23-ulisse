package scala.core

import scala.core.Route.{Path, TypeRoute}

trait Route:
  def typology: TypeRoute
  def railsCount: Int
  def path: Path

object Route:
  def apply(typeRoute: TypeRoute, railsCount: Int, path: Path): Route =
    RouteImpl(typeRoute, railsCount, path)

  type Path = (String, String)

  enum TypeRoute:
    case Normal, AV

  private case class RouteImpl(typology: TypeRoute, railsCount: Int, path: Path)
      extends Route
