package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager
import ulisse.applications.useCases.RouteManager.ErrorSaving
import ulisse.entities.Route
import ulisse.entities.Route.Id

object RoutePorts:

  trait UIPort():
    def size: Int
    def routes: List[Route]
    def route(id: Id): Option[Route]

    def save(route: Option[Route]): Either[ErrorSaving, UIPort]
