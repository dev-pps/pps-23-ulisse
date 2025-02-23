package ulisse.infrastructures.view.dashboard
import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.ComponentMixins.UpdatableContainer
import ulisse.infrastructures.view.components.container.LayeredContainers.*

import java.awt.BorderLayout
import scala.swing.{BorderPanel, Component}

final case class Dashboard(root: UpdatableContainer) extends JLayeredPane with UpdatableContainer:
  private val defaultLayerLayout = new BorderPanel()

  private val sideMenu = SideMenu(this)

  override def update(component: Component): Unit =
    defaultLayerLayout.layout(component) = BorderPanel.Position.Center
    revalidate()
    repaint()

  mainPane.peer.add(defaultLayerLayout.peer)
  glassPane.peer.add(sideMenu.peer, BorderLayout.WEST)
