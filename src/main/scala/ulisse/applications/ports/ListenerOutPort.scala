package ulisse.applications.ports

import ulisse.applications.useCases.RouteManager

trait ListenerOutPort:
  def onSubmit(values: List[String]): Unit

object ListenerOutPort:

  def apply(routeBank: RouteManager): ListenerOutPort =
    ListenerOutPortImpl(routeBank)

  private case class ListenerOutPortImpl(routeBank: RouteManager)
      extends ListenerOutPort:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var bank: RouteManager = routeBank

    override def onSubmit(values: List[String]): Unit = println("submit")
//      bank.save(Route(values(0), values(1), values(2)))
