package view

import application.RouteBank

trait ListenerAdapter

object ListenerAdapter:

  def apply(routeBank: RouteBank): ListenerAdapter =
    ListenerAdapterImpl(routeBank)

  private case class ListenerAdapterImpl(routeBank: RouteBank)
      extends ListenerAdapter
