package ulisse.infrastructures.view.dashboard
import scaredOfArchunit.StationSettings
import ulisse.infrastructures.view.UpdatableContainer
import ulisse.infrastructures.view.components.ComponentUtils.*
import java.awt.Color
import scala.swing.event.MouseClicked
import scala.swing.{BorderPanel, BoxPanel, Dimension, Label, Orientation, Swing}

final case class Dashboard(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)

  val menuBar = MenuBar().fixedSize(600, 50)
  layout(menuBar) = BorderPanel.Position.North

  val sideMenu = SideMenu()
  sideMenu.background = Color.GREEN

  layout(sideMenu) = BorderPanel.Position.West
  layout(StationSettings().stationEditorView) = BorderPanel.Position.Center
