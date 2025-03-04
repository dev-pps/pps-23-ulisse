package ulisse.applications.managers

import cats.syntax.either.*
import ulisse.entities.route.Routes.{IdRoute, Route}
import ulisse.utils.Errors.ErrorMessage

import scala.collection.immutable.Map

/** Define the manager of the routes, and the errors that can be generated during the management of the routes */
object RouteManagers:

  /** Errors that can be generated during the management of the routes */
  enum Errors(val text: String) extends ErrorMessage(text):

    /** Route already exists */
    case AlreadyExist extends Errors("Route already exists")

    /** Route does not exist */
    case NotExist extends Errors("Route does not exist")

    /** Route not found */
    case NotFound extends Errors("Route not found")

  /** Manages the routes of the train network. */
  trait RouteManager:
    // delete from train
    // modify from train
    // route accept a specific train: Train => Boolean
    // route from departure station
    // route from arrival station
    // route that contain a specific station

    /** Bank of routes */
    opaque type Bank = Map[IdRoute, Route]

    /** Number of routes */
    def size: Int

    /** List of routes */
    def routes: List[Route]

    /** Check if the route is present in the manager */
    def contains(route: Route): Boolean

    /** Find a route by its identifier */
    def findBy(id: IdRoute): Either[Errors, Route]

    /** Find a route */
    def find(route: Route): Either[Errors, Route] = findBy(route.id)

    /** Save a route */
    def save(route: Route): Either[Errors, RouteManager]

    /** Modify a route */
    def modify(oldRoute: Route, newRoute: Route): Either[Errors, RouteManager]

    /** Delete a route by its identifier */
    def deleteBy(id: IdRoute): Either[Errors, RouteManager]

    /** Delete a route */
    def delete(route: Route): Either[Errors, RouteManager] = deleteBy(route.id)

  /** Companion object of [[RouteManager]] */
  object RouteManager:

    /** Create a new [[RouteManager]] */
    def apply(bank: Map[IdRoute, Route]): RouteManager = RouteManagerImpl(bank)

    /** Create an empty [[RouteManager]] */
    def empty(): RouteManager = RouteManager(Map.empty)

    /** Create a [[RouteManager]] from a list of routes */
    def createOf(routes: List[Route]): RouteManager = RouteManager(routes.map(route => (route.id, route)).toMap)

    private case class RouteManagerImpl(manager: Map[IdRoute, Route]) extends RouteManager:
      export manager.size
      override def routes: List[Route]                        = manager.values.toList
      override def contains(route: Route): Boolean            = manager contains route.id
      override def findBy(id: IdRoute): Either[Errors, Route] = manager get id toRight Errors.NotFound

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
