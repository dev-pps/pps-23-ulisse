package applications.ports

import applications.RouteBank

trait ListenerOutPort:
  def onSubmit(values: List[String]): Unit

object ListenerOutPort:

  def apply(routeBank: RouteBank): ListenerOutPort =
    ListenerOutPortImpl(routeBank)

  private case class ListenerOutPortImpl(routeBank: RouteBank)
      extends ListenerOutPort:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var bank: RouteBank = routeBank

    override def onSubmit(values: List[String]): Unit = println("submit")
//      bank.save(Route(values(0), values(1), values(2)))
