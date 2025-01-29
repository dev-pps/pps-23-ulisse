package ulisse.infrastructures.view

import scala.swing.*

trait Updatable:
  this: MainFrame | LayoutContainer =>
  def update(component: Component): Unit

trait BaseView:
  val updatable: Updatable

final case class StationEditorView(updatable: Updatable) extends BorderPanel with BaseView with Updatable:
  def update(component: Component): Unit =
    layout(component) = BorderPanel.Position.Center
    repaint()

final case class AppFrame() extends MainFrame with Updatable:
  def update(component: Component): Unit =
    contents = component
    repaint()
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  pack()
  centerOnScreen()
