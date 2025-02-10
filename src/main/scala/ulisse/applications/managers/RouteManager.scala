package ulisse.applications.managers

import cats.syntax.either.*
import ulisse.applications.managers.RouteManager.ErrorSaving
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.{IdRoute, Route}

import scala.collection.immutable.Map

trait RouteManager[N: Numeric, C <: Coordinate[N]]:
  opaque type Bank = Map[IdRoute, Route[N, C]]
  def size: Int
  def routes: List[Route[N, C]]
  def contains(route: Route[N, C]): Boolean

  def find(id: IdRoute): Option[Route[N, C]]
  def save(route: Route[N, C]): Either[ErrorSaving, RouteManager[N, C]]

object RouteManager:
  def apply[N: Numeric, C <: Coordinate[N]](bank: Map[IdRoute, Route[N, C]]): RouteManager[N, C] =
    RouteManagerImpl(bank)
  def empty[N: Numeric, C <: Coordinate[N]](): RouteManager[N, C] = RouteManager(Map.empty)
  def createOf[N: Numeric, C <: Coordinate[N]](routes: List[Route[N, C]]): RouteManager[N, C] =
    RouteManager(routes.map(route => (route.id, route)).toMap)

  enum ErrorSaving:
    case notExist
    case creation

  private case class RouteManagerImpl[N: Numeric, C <: Coordinate[N]](manager: Map[IdRoute, Route[N, C]])
      extends RouteManager[N, C]:
    override def size: Int                             = manager.size
    override def routes: List[Route[N, C]]             = manager.values.toList
    override def contains(route: Route[N, C]): Boolean = manager.contains(route.id)

    override def find(id: IdRoute): Option[Route[N, C]] = manager.get(id)

    override def save(route: Route[N, C]): Either[ErrorSaving, RouteManager[N, C]] =
      this.copy(manager + (route.id -> route)).asRight
