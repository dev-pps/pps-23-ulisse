package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager
import ulisse.applications.useCases.RouteManager.ErrorSaving
import ulisse.entities.Route
import ulisse.entities.Route.Id

import scala.concurrent.Future

object RoutePorts:

  trait UIPort():
    def save(route: Option[Route]): Future[Either[ErrorSaving, List[Route]]]
