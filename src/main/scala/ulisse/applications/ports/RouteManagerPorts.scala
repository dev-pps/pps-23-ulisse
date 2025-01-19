package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route

object RouteManagerPorts:

  trait Input:
    def save(route: Route): RouteManager

  trait Output
