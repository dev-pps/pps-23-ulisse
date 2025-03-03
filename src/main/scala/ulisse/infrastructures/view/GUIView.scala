package ulisse.infrastructures.view

import ulisse.applications.InputPortManager
import ulisse.infrastructures.view.manager.PageManager
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.*
import scala.swing.BorderPanel.Position.*

trait GUIView

object GUIView:
  def apply(inputPortManager: InputPortManager): GUIView = GUIViewImpl(inputPortManager)

  private case class GUIViewImpl(inputPortManager: InputPortManager) extends MainFrame, GUIView:
    title = "Map"
    visible = true
    preferredSize = new Dimension(1400, 1000)

    private val pageManager = PageManager()

    contents = pageManager.component
    pageManager.revalidate()
