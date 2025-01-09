package scala.view

import scala.swing.BorderPanel

trait RouteCreationPanel

object RouteCreationPanel:
  def apply(): RouteCreationPanel = RouteCreationPanelImpl()

  private case class RouteCreationPanelImpl() extends BorderPanel,
        RouteCreationPanel:
