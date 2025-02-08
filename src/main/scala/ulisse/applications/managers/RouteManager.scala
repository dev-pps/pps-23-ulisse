package ulisse.applications.managers

import cats.syntax.either.*
import RouteManager.ErrorSaving
import ulisse.entities.Routes.{IdRoute, Route}

import scala.collection.immutable.Map

trait RouteManager:
  def size: Int
  def routes: List[Route]
  def contains(route: Route): Boolean

  def find(id: IdRoute): Option[Route]
  def save(route: Route): Either[ErrorSaving, RouteManager]

object RouteManager:
  def apply(bank: Bank): RouteManager             = RouteManagerImpl(bank)
  def empty(): RouteManager                       = RouteManager(Map.empty)
  def createOf(routes: List[Route]): RouteManager = RouteManager(routes.map(route => (route.id, route)).toMap)

  enum ErrorSaving:
    case notExist
    case creation

  opaque type Bank = Map[IdRoute, Route]

  private case class RouteManagerImpl(manager: Bank) extends RouteManager:
    override def size: Int                       = manager.size
    override def routes: List[Route]             = manager.values.toList
    override def contains(route: Route): Boolean = manager.contains(route.id)

    override def find(id: IdRoute): Option[Route] = manager.get(id)

    override def save(route: Route): Either[ErrorSaving, RouteManager] =
      this.copy(manager + (route.id -> route)).asRight
