package ulisse.infrastructures.view

import ulisse.applications.ports.RoutePorts.Input
import ulisse.infrastructures.view.manager.PageManager
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.*
import scala.swing.BorderPanel.Position.*

trait GUIView

object GUIView:
  def apply(): GUIView = GUIViewImpl()

  private case class GUIViewImpl() extends MainFrame, GUIView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(1400, 1000)

    private val pageManager = PageManager()

    contents = pageManager.component
    pageManager.revalidate()
