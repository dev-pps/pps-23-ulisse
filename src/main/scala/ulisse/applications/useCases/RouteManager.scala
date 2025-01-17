package ulisse.applications.useCases

import ulisse.entities.Route
import ulisse.entities.Route.Id

import scala.collection.immutable.Map

trait RouteManager:
  def size: Int
  def route(id: Id): Option[Route]
  def save(route: Route): RouteManager
  def contains(route: Route): Boolean

object RouteManager:
  def apply(bank: Bank): RouteManager = RouteManagerImpl(bank)
  def fromList(routes: List[Route]): RouteManager =
    RouteManagerImpl(routes.map(route => (route.id, route)).toMap)
  def empty(): RouteManager = RouteManagerImpl(Map.empty)

  opaque type Bank = Map[Id, Route]

  private case class RouteManagerImpl(bank: Bank) extends RouteManager:
    override def size: Int = bank.size

    override def save(route: Route): RouteManager =
      RouteManager(bank + (route.id -> route))

    override def route(id: Id): Option[Route] = bank.get(id)

    override def contains(route: Route): Boolean = bank.contains(route.id)
