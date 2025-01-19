package ulisse.applications.useCases

import cats.syntax.either.*
import ulisse.applications.useCases.RouteManager.ErrorSaving
import ulisse.entities.Route
import ulisse.entities.Route.Id
import ulisse.utils.CreationUtils

import scala.collection.immutable.Map

trait RouteManager:
  def size: Int
  def route(id: Id): Option[Route]
  def save(route: Route): Either[ErrorSaving, RouteManager]
  def contains(route: Route): Boolean

object RouteManager:
  def apply(bank: Bank): RouteManager = RouteManagerImpl(bank)
  def empty(): RouteManager           = RouteManager(Map.empty)
  def createOf(routes: List[Route]): RouteManager =
    RouteManager(routes.map(route => (route.id, route)).toMap)

  enum ErrorSaving:
    case notExist

  opaque type Bank = Map[Id, Route]

  private case class RouteManagerImpl(manager: Bank) extends RouteManager:
    override def size: Int = manager.size

    override def save(route: Route): Either[ErrorSaving, RouteManager] =
      CreationUtils.updateWith(this)(_.copy(manager + (route.id -> route))).asRight

    override def route(id: Id): Option[Route] = manager.get(id)

    override def contains(route: Route): Boolean = manager.contains(route.id)
