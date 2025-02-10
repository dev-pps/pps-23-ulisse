package ulisse.applications.ports

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.RouteManagers.Errors
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route

import scala.concurrent.Future

object RoutePorts:

  trait UIInputPort[N: Numeric, C <: Coordinate[N]]:
    def save(route: Option[Route[N, C]]): Future[Either[Errors, List[Route[N, C]]]]
