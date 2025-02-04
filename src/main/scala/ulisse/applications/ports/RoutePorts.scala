package ulisse.applications.ports

import ulisse.applications.managers.RouteManager
import RouteManager.ErrorSaving
import ulisse.entities.Route
import ulisse.entities.Route.Id

import scala.concurrent.Future

object RoutePorts:

  trait UIInputPort():
    def save(route: Option[Route]): Future[Either[ErrorSaving, List[Route]]]
