package scala.application

import scala.core.Route
import scala.collection.immutable.{ArraySeq, List}
import scala.core.Route.Id

trait RouteBank:

  def save(route: Route): RouteBank
  def contains(route: Route): Boolean
  def route(id: Id): Option[Route]

object RouteBank:
  def apply(routes: List[Route]): RouteBank = RouteBankImpl(routes)
  def empty(): RouteBank                    = RouteBankImpl(List.empty[Route])

  opaque type Bank = List[Route]

  private case class RouteBankImpl(bank: Bank) extends RouteBank:
    private val containsFunction: PartialFunction[Route, Bank] =
      case x if contains(x) => bank

    override def save(route: Route): RouteBank =
      RouteBank(containsFunction.applyOrElse(route, bank.appended))

    override def contains(route: Route): Boolean = bank.contains(route)

    override def route(id: Id): Option[Route] = bank.find(_ has id)
