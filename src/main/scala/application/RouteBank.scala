package scala.application

import scala.collection.immutable.Map
import scala.core.Route
import scala.core.Route.Id

trait RouteBank:

  def save(route: Route): RouteBank
  def contains(route: Route): Boolean
  def route(id: Id): Option[Route]

object RouteBank:
  def apply(bank: Bank): RouteBank = RouteBankImpl(bank)
  def fromList(routes: List[Route]): RouteBank =
    RouteBankImpl(routes.map(route => (route.id, route)).toMap)
  def empty(): RouteBank = RouteBankImpl(Map.empty)

  opaque type Bank = Map[Id, Route]

  private case class RouteBankImpl(bank: Bank) extends RouteBank:

    override def save(route: Route): RouteBank =
      RouteBank(bank + (route.id -> route))

    override def route(id: Id): Option[Route] = bank.get(id)

    override def contains(route: Route): Boolean = bank.contains(route.id)
