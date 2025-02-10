package ulisse.applications.ports

import ulisse.applications.managers.RouteManager
import ulisse.applications.managers.RouteManager.ErrorSaving
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.Routes.Route

import scala.concurrent.Future

object RoutePorts:

  trait UIInputPort[N: Numeric, C <: Coordinate[N]]:
    def save(route: Option[Route[N, C]]): Future[Either[ErrorSaving, List[Route[N, C]]]]
