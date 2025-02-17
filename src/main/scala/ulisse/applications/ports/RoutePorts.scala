package ulisse.applications.ports

import ulisse.applications.managers.RouteManagers.Errors
import ulisse.entities.route.Routes.Route

import scala.concurrent.Future

object RoutePorts:

  trait Input:
    def save(route: Route): Future[Either[Errors, List[Route]]]
    def modify(oldRoute: Route, newRoute: Route): Future[Either[Errors, List[Route]]]
    def delete(route: Route): Future[Either[Errors, List[Route]]]

  trait Output
