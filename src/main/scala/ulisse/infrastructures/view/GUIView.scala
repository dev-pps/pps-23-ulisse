package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.Input
import ulisse.infrastructures.view.manager.PageManager
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.*
import scala.swing.BorderPanel.Position.*

trait GUIView

object GUIView:
  def apply(uiPort: Input): GUIView = GUIViewImpl(uiPort)

  private case class GUIViewImpl(uiPort: Input) extends MainFrame, GUIView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(1000, 1000)

    private val pageManager = PageManager()

    contents = pageManager.component
    pageManager.revalidate()
