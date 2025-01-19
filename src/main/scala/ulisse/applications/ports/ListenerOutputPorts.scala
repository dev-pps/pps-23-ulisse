package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager

trait ListenerOutputPorts:
  def onSubmit(values: List[String]): Unit

object ListenerOutputPorts:

  def apply(routeBank: RouteManager): ListenerOutputPorts =
    ListenerOutputPortsImpl(routeBank)

  private case class ListenerOutputPortsImpl(routeBank: RouteManager)
      extends ListenerOutputPorts:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var bank: RouteManager = routeBank

    override def onSubmit(values: List[String]): Unit = println("submit")
//      bank.save(Route(values(0), values(1), values(2)))
