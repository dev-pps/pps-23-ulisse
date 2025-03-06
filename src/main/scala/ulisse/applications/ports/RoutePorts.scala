package ulisse.applications.ports

import ulisse.applications.managers.RouteManagers.Errors
import ulisse.entities.route.Routes.Route

import scala.concurrent.Future

/** Ports for the route management. */
object RoutePorts:

  /** Input port for the route management. */
  trait Input:
    /** Save a route. */
    def save(route: Route): Future[Either[Errors, List[Route]]]

    /** Modify a route, replacing the old route with the new route. */
    def modify(oldRoute: Route, newRoute: Route): Future[Either[Errors, List[Route]]]

    /** Delete a route. */
    def delete(route: Route): Future[Either[Errors, List[Route]]]
