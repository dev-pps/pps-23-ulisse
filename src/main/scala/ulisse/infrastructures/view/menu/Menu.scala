package ulisse.infrastructures.view.menu

import scaredOfArchunit.StationSettings
import ulisse.infrastructures.view.UpdatableContainer
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.dashboard.{Dashboard, MenuBar, SideMenu}

import java.awt.Color
import scala.swing.event.MouseClicked
import scala.swing.{BorderPanel, BoxPanel, Dimension, Label, Orientation, Swing}

final case class AppMenu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  val card = Example.drawnCardExample
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(Dashboard(root)))

  val card2 = Example.svgCardExample
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(Dashboard(root)))

  val card3 = Example.imageCardExample
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(Dashboard(root)))

  private val centerPanel = new BoxPanel(Orientation.Vertical):
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += card
      contents += Swing.HStrut(10)
      contents += card2
      contents += Swing.HStrut(10)
      contents += card3
      contents += Swing.HGlue
    contents += Swing.VGlue
  layout(centerPanel) = BorderPanel.Position.Center
