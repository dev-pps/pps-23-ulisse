package ulisse.infrastructures.view

import ulisse.infrastructures.view.menu.Menu

import scala.swing.*

trait UpdatableContainer:
  this: MainFrame | LayoutContainer =>
  def update(component: Component): Unit

trait BaseView:
  val updatable: UpdatableContainer

final case class StationEditorView(updatable: UpdatableContainer) extends BorderPanel with BaseView
    with UpdatableContainer:
  def update(component: Component): Unit =
    layout(component) = BorderPanel.Position.Center
    repaint()

final case class AppFrame() extends MainFrame with UpdatableContainer:
  def update(component: Component): Unit =
    contents = component
    repaint()
  title = "Station Editor"
  minimumSize = new Dimension(400, 300)
  preferredSize = new Dimension(800, 600)
  pack()
  centerOnScreen()

@main def testLayout(): Unit =
  val app  = AppFrame()
  val view = Menu(app)
  app.contents = view
  app.open()
