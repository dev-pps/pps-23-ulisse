package ulisse.applications.managers

import cats.syntax.either.*
import ulisse.entities.route.Routes.{IdRoute, Route}
import ulisse.utils.Errors.ErrorMessage

import scala.collection.immutable.Map

object RouteManagers:
  enum Errors(val text: String) extends ErrorMessage(text):
    case AlreadyExist extends Errors("Route already exists")
    case NotExist     extends Errors("Route does not exist")
    case NotFound     extends Errors("Route not found")

  trait RouteManager:
    opaque type Bank = Map[IdRoute, Route]
    def size: Int
    def routes: List[Route]
    def contains(route: Route): Boolean

    def findBy(id: IdRoute): Either[Errors, Route]
    def find(route: Route): Either[Errors, Route] = findBy(route.id)

    def save(route: Route): Either[Errors, RouteManager]
    def modify(oldRoute: Route, newRoute: Route): Either[Errors, RouteManager]

    def deleteBy(id: IdRoute): Either[Errors, RouteManager]
    def delete(route: Route): Either[Errors, RouteManager] = deleteBy(route.id)

  object RouteManager:
    def apply(bank: Map[IdRoute, Route]): RouteManager =
      RouteManagerImpl(bank)
    def empty(): RouteManager = RouteManager(Map.empty)
    def createOf(routes: List[Route]): RouteManager =
      RouteManager(routes.map(route => (route.id, route)).toMap)

    private case class RouteManagerImpl(manager: Map[IdRoute, Route])
        extends RouteManager:
      export manager.size
      override def routes: List[Route]                        = manager.values.toList
      override def contains(route: Route): Boolean            = manager.contains(route.id)
      override def findBy(id: IdRoute): Either[Errors, Route] = manager.get(id).toRight(Errors.NotFound)

      override def save(route: Route): Either[Errors, RouteManager] =
        findBy(route.id).map(_ => Errors.AlreadyExist.asLeft).getOrElse(copy(manager + (route.id -> route)).asRight)

      override def modify(oldRoute: Route, newRoute: Route): Either[Errors, RouteManager] =
        val managerWithoutOldRoute = copy(manager - oldRoute.id)
        (findBy(oldRoute.id), managerWithoutOldRoute.findBy(newRoute.id)) match
          case (Left(error), _) => error.asLeft
          case (_, Right(_))    => Errors.AlreadyExist.asLeft
          case (_, _)           => copy(manager - oldRoute.id + (newRoute.id -> newRoute)).asRight

      override def deleteBy(id: IdRoute): Either[Errors, RouteManager] =
        findBy(id).map(_ => copy(manager - id).asRight).getOrElse(Errors.NotExist.asLeft)
