package ulisse.applications.adapters

import ulisse.applications.ports.RouteManagerPorts
import ulisse.applications.useCases.RouteManager
import ulisse.entities.Route

object RouteManagerInputAdapter:
  def apply(manager: RouteManager): RouteManagerPorts.Input = RouteManagerInputAdapterImpl(manager)

  private case class RouteManagerInputAdapterImpl(manager: RouteManager) extends RouteManagerPorts.Input(manager):
    override def save(route: Route): Unit = manager.save(route)
