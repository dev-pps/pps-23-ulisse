package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route

object RouteManagerPorts:

  trait Input(routeManager: RouteManager):
    def save(route: Route): Unit

  trait Output
