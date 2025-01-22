package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route

object RoutePorts:

  trait UIPort():
    def size: Int
    def save(route: Option[Route]): Either[RouteManager.ErrorSaving, UIPort]
