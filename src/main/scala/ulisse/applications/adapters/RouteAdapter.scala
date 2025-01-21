package ulisse.applications.adapters

import cats.syntax.either.*
import ulisse.applications.ports.RoutePorts
import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route

object RouteAdapter:

  object UIAdapter:
    def apply(manager: RouteManager): RoutePorts.UIPort = IUAdapter(manager)

    private case class IUAdapter(manager: RouteManager) extends RoutePorts.UIPort():
      override def size: Int = manager.size

      override def save(route: Route): Either[RouteManager.ErrorSaving, RoutePorts.UIPort] =
        manager.save(route).map(UIAdapter(_))
