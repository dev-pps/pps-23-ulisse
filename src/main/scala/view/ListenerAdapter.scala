package view

import application.RouteBank

trait ListenerAdapter:
  def onSubmit(values: List[String]): Unit

object ListenerAdapter:

  def apply(routeBank: RouteBank): ListenerAdapter =
    ListenerAdapterImpl(routeBank)

  private case class ListenerAdapterImpl(routeBank: RouteBank)
      extends ListenerAdapter:
    @SuppressWarnings(Array("org.wartremover.warts.Var"))
    private var bank: RouteBank = routeBank

    override def onSubmit(values: List[String]): Unit = println("submit")
//      bank.save(Route(values(0), values(1), values(2)))
