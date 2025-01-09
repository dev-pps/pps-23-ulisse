package scala.application

import scala.core.Route
import scala.collection.immutable.{ ArraySeq, List }

trait RouteBank:
  def save(route: Route): RouteBank
  def contains(route: Route): Boolean

object RouteBank:
  def apply(routes: List[Route]): RouteBank = RouteBankImpl(routes)
  def empty(): RouteBank = RouteBankImpl(List.empty[Route])


  opaque type Bank = List[Route]

  private case class RouteBankImpl(bank: Bank) extends RouteBank:
    override def save(route: Route): RouteBank = RouteBank(bank.appended(route))
    override def contains(route: Route): Boolean = bank.contains(route)
    
    