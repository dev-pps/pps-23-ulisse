package ulisse.infrastructures.view

import ulisse.applications.InputPortManager
import ulisse.infrastructures.view.manager.{PageManager, WorkspaceManager}
import ulisse.infrastructures.view.page.{Dashboard, Menu}
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.swing.BorderPanel.Position.*
import scala.swing.{Dimension, MainFrame}

trait GUIView

object GUIView:
  def apply(inputPortManager: InputPortManager): GUIView = GUIViewImpl(inputPortManager)

  private case class GUIViewImpl(inputPortManager: InputPortManager) extends MainFrame, GUIView:
    title = "Ulisse"
    visible = true
    preferredSize = new Dimension(1600, 1000)

    private val menu             = Menu()
    private val dashboard        = Dashboard()
    private val workspaceManager = WorkspaceManager()

    private val pageManager = PageManager(menu, dashboard, workspaceManager)

    contents = pageManager.component
    pageManager.revalidate()
