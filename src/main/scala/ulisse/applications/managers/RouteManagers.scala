package ulisse.applications.managers

import cats.syntax.either.*
import cats.syntax.option.*
import ulisse.entities.route.Routes.{IdRoute, Route}
import ulisse.entities.station.Station
import ulisse.utils.Errors.ErrorMessage

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
    // modify from station

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

    /** Find all the routes that depart from a specific station. */
    def findByDeparture(station: Station): Either[Errors, List[Route]] =
      (routes filter (_ isDeparture station)).some filter (_.nonEmpty) toRight Errors.NotFound

    /** Find all the routes that arrive at a specific station. */
    def findByArrival(station: Station): Either[Errors, List[Route]] =
      (routes filter (_ isArrival station)).some filter (_.nonEmpty) toRight Errors.NotFound

    /** Find all the routes that contain a specific path. */
    def findByPath(a: Station, b: Station): Either[Errors, List[Route]] =
      (routes filter (_ isPath (a, b))).some filter (_.nonEmpty) toRight Errors.NotFound

    /** Save a route */
    def save(route: Route): Either[Errors, RouteManager]

    /** Modify a route */
    def modify(oldRoute: Route, newRoute: Route): Either[Errors, RouteManager]

    /** Delete a route by its identifier */
    def deleteBy(id: IdRoute): Either[Errors, RouteManager]

    /** Delete a route */
    def delete(route: Route): Either[Errors, RouteManager] = deleteBy(route.id)

    /** Delete all the routes that depart from a specific station. */
    def deleteByDeparture(station: Station): Either[Errors, RouteManager]

    /** Delete all the routes that arrive at a specific station. */
    def deleteByArrival(station: Station): Either[Errors, RouteManager]

    /** Delete all the routes that contain this station, either as departure or arrival. */
    def deleteByStation(station: Station): Either[Errors, RouteManager] =
      deleteByDeparture(station) flatMap (_ deleteByArrival station)

  /** Companion object of [[RouteManager]] */
  object RouteManager:

    /** Create a new [[RouteManager]] */
    def apply(bank: Map[IdRoute, Route]): RouteManager = RouteManagerImpl(bank)

    /** Create an empty [[RouteManager]] */
    def empty(): RouteManager = RouteManager(Map.empty)

    /** Create a [[RouteManager]] from a list of routes */
    def createOf(routes: List[Route]): RouteManager = RouteManager(routes.map(route => route.id -> route).toMap)

    private case class RouteManagerImpl(manager: Map[IdRoute, Route]) extends RouteManager:
      export manager.size

      override def routes: List[Route]                        = manager.values.toList
      override def contains(route: Route): Boolean            = manager contains route.id
      override def findBy(id: IdRoute): Either[Errors, Route] = manager get id toRight Errors.NotFound

      override def save(route: Route): Either[Errors, RouteManager] =
        find(route) map (_ => Errors.AlreadyExist.asLeft) getOrElse copy(manager + (route.id -> route)).asRight

      override def modify(oldRoute: Route, newRoute: Route): Either[Errors, RouteManager] =
        val managerWithoutOldRoute = copy(manager - oldRoute.id)
        (findBy(oldRoute.id), managerWithoutOldRoute findBy newRoute.id) match
          case (Left(error), _) => error.asLeft
          case (_, Right(_))    => Errors.AlreadyExist.asLeft
          case (_, _)           => copy(manager - oldRoute.id + (newRoute.id -> newRoute)).asRight

      override def deleteBy(id: IdRoute): Either[Errors, RouteManager] =
        findBy(id).map(_ => copy(manager - id).asRight).getOrElse(Errors.NotExist.asLeft)

      private def deleteByStation(find: Station => Either[Errors, List[Route]])
          : Station => Either[Errors, RouteManager] = station =>
        find(station) flatMap (_.foldLeft[Either[Errors, RouteManager]](this.asRight)((acc, route) =>
          acc flatMap (_ delete route)
        ))

      override def deleteByDeparture(station: Station): Either[Errors, RouteManager] =
        deleteByStation(findByDeparture)(station)

      override def deleteByArrival(station: Station): Either[Errors, RouteManager] =
        deleteByStation(findByArrival)(station)
