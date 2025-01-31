package ulisse.infrastructures.view.menu

import scaredOfArchunit.StationSettings
import ulisse.infrastructures.view.UpdatableContainer
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.dashboard.{MenuBar, SideMenu}

import java.awt.Color
import scala.swing.event.MouseClicked
import scala.swing.{BorderPanel, BoxPanel, Dimension, Label, Orientation, Swing}

final case class AppMenu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  val card = Example.drawnCardExample
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(StationSettings().stationEditorView))

  val card2 = Example.svgCardExample
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(StationSettings().stationEditorView))

  val card3 = Example.imageCardExample
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(StationSettings().stationEditorView))

  val menuBar = MenuBar().fixedSize(600, 50)
  layout(menuBar) = BorderPanel.Position.North

  val sideMenu = SideMenu() // .fixedSize(100, 400)
  sideMenu.background = Color.GREEN
//  val sideMenu = new BoxPanel(Orientation.Vertical) {
//    background = Color.DARK_GRAY
//    contents += Swing.VStrut(10)
//    contents += new Label("Side Menu")
//    contents += Swing.VStrut(10)
//  }.fixedSize(100, 400)

  layout(sideMenu) = BorderPanel.Position.West

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
