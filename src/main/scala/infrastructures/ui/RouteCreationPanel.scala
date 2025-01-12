<<<<<<<< HEAD:src/main/scala/infrastructure/ui/RouteCreationPanel.scala
package infrastructure.ui
========
package infrastructures.ui
>>>>>>>> 87a158c (refactor: create hexagonal hierarchy packages):src/main/scala/infrastructures/ui/RouteCreationPanel.scala

import scala.swing.BorderPanel

trait RouteCreationPanel

object RouteCreationPanel:
  def apply(): RouteCreationPanel = RouteCreationPanelImpl()

  private case class RouteCreationPanelImpl() extends BorderPanel,
        RouteCreationPanel
