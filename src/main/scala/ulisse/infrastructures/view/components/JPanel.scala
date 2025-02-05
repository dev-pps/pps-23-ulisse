package ulisse.infrastructures.view.components

import scala.swing.*

trait JPanel

object JPanel:
  def createFlow(): Flow                             = Flow().defaultPanelSettings()
  def createBox(orientation: Orientation.Value): Box = Box(orientation).defaultPanelSettings()
  def createGrid(row: Int, colum: Int): Grid         = Grid(row, colum).defaultPanelSettings()
  def createBorder(): Border                         = Border().defaultPanelSettings()
  def createGridBag(): GridBag                       = GridBag().defaultPanelSettings()

  extension [T <: Panel](panel: T)
    def defaultPanelSettings(): T =
      panel.opaque = false
      panel

  case class Flow()                              extends FlowPanel
  case class Box(orientation: Orientation.Value) extends BoxPanel(orientation)
  case class Grid(row: Int, colum: Int)          extends GridPanel(row, colum)
  case class Border()                            extends BorderPanel
  case class GridBag()                           extends GridBagPanel
