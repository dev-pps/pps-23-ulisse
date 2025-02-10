package ulisse.applications.managers

import cats.syntax.either.*
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.{IdRoute, Route}
import ulisse.utils.Errors.ErrorMessage

import scala.collection.immutable.Map

object RouteManagers:
  enum Errors(val text: String) extends ErrorMessage(text):
    case AlreadyExist extends Errors("Route already exists")
    case NotExist     extends Errors("Route does not exist")
    case NotFound     extends Errors("Route not found")

  trait RouteManager[N: Numeric, C <: Coordinate[N]]:
    opaque type Bank = Map[IdRoute, Route[N, C]]
    def size: Int
    def routes: List[Route[N, C]]
    def contains(route: Route[N, C]): Boolean
    def find(id: IdRoute): Either[Errors, Route[N, C]]

    def save(route: Route[N, C]): Either[Errors, RouteManager[N, C]]
    def modify(oldRoute: Route[N, C], newRoute: Route[N, C]): Either[Errors, RouteManager[N, C]]
    def delete(id: IdRoute): Either[Errors, RouteManager[N, C]]

  object RouteManager:
    def apply[N: Numeric, C <: Coordinate[N]](bank: Map[IdRoute, Route[N, C]]): RouteManager[N, C] =
      RouteManagerImpl(bank)
    def empty[N: Numeric, C <: Coordinate[N]](): RouteManager[N, C] = RouteManager(Map.empty)
    def createOf[N: Numeric, C <: Coordinate[N]](routes: List[Route[N, C]]): RouteManager[N, C] =
      RouteManager(routes.map(route => (route.id, route)).toMap)

    private case class RouteManagerImpl[N: Numeric, C <: Coordinate[N]](manager: Map[IdRoute, Route[N, C]])
        extends RouteManager[N, C]:
      export manager.size
      override def routes: List[Route[N, C]]                      = manager.values.toList
      override def contains(route: Route[N, C]): Boolean          = manager.contains(route.id)
      override def find(id: IdRoute): Either[Errors, Route[N, C]] = manager.get(id).toRight(Errors.NotFound)

      override def save(route: Route[N, C]): Either[Errors, RouteManager[N, C]] =
        find(route.id).map(_ => Errors.AlreadyExist.asLeft).getOrElse(copy(manager + (route.id -> route)).asRight)

      override def modify(oldRoute: Route[N, C], newRoute: Route[N, C]): Either[Errors, RouteManager[N, C]] =
        (find(oldRoute.id), find(newRoute.id)) match
          case (Left(error), _) => error.asLeft
          case (_, Right(_))    => Errors.AlreadyExist.asLeft
          case (_, _)           => copy(manager.removed(oldRoute.id) + (newRoute.id -> newRoute)).asRight

      override def delete(id: IdRoute): Either[Errors, RouteManager[N, C]] =
        find(id).map(_ => copy(manager - id).asRight).getOrElse(Errors.NotExist.asLeft)
