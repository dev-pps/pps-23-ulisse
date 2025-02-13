package ulisse.applications.ports

import ulisse.applications.managers.RouteManagers.Errors
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route

import scala.concurrent.Future

object RoutePorts:

  trait Input[N: Numeric, C <: Coordinate[N]]:
    def save(route: Route[N, C]): Future[Either[Errors, List[Route[N, C]]]]
    def modify(oldRoute: Route[N, C], newRoute: Route[N, C]): Future[Either[Errors, List[Route[N, C]]]]
    def delete(route: Route[N, C]): Future[Either[Errors, List[Route[N, C]]]]

  trait Output[N: Numeric, C <: Coordinate[N]]
