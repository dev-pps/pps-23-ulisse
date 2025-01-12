package applications

import entities.Route
import entities.Route.Id

import scala.collection.immutable.Map

trait RouteBank:
  def size: Int
  def route(id: Id): Option[Route]
  def save(route: Route): RouteBank
  def contains(route: Route): Boolean

object RouteBank:
  def apply(bank: Bank): RouteBank = RouteBankImpl(bank)
  def fromList(routes: List[Route]): RouteBank =
    RouteBankImpl(routes.map(route => (route.id, route)).toMap)
  def empty(): RouteBank = RouteBankImpl(Map.empty)

  opaque type Bank = Map[Id, Route]

  private case class RouteBankImpl(bank: Bank) extends RouteBank:
    override def size: Int = bank.size

    override def save(route: Route): RouteBank =
      RouteBank(bank + (route.id -> route))

    override def route(id: Id): Option[Route] = bank.get(id)

    override def contains(route: Route): Boolean = bank.contains(route.id)
