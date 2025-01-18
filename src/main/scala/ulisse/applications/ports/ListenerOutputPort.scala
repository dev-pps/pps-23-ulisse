package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager

trait ListenerOutputPort:
  def onSubmit(values: List[String]): Unit

object ListenerOutputPort:

  def apply(routeBank: RouteManager): ListenerOutputPort =
    ListenerOutputPortImpl(routeBank)

  private case class ListenerOutputPortImpl(routeBank: RouteManager)
      extends ListenerOutputPort:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var bank: RouteManager = routeBank

    override def onSubmit(values: List[String]): Unit = println("submit")
//      bank.save(Route(values(0), values(1), values(2)))
