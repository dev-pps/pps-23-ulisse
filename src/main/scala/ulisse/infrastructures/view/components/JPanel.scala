package ulisse.infrastructures.view.components

import java.awt.FlowLayout
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

  case class Flow() extends FlowPanel with JPanel:
    private val layout = new FlowLayout(FlowLayout.CENTER, 0, 0)
    peer.setLayout(layout)

  case class Box(orientation: Orientation.Value) extends BoxPanel(orientation) with JPanel
  case class Grid(row: Int, colum: Int)          extends GridPanel(row, colum) with JPanel
  case class Border()                            extends BorderPanel with JPanel
  case class GridBag()                           extends GridBagPanel with JPanel
