package ulisse.applications.ports

import ulisse.entities.Route

object RoutePorts:

  trait UIPort():
    def save(route: Route): Either[String, Route]
