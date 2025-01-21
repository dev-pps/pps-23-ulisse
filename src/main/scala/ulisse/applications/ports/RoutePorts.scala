package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route

object RoutePorts:

  trait UIPort():
    def size: Int
    def save(route: Route): Either[RouteManager.ErrorSaving, UIPort]
