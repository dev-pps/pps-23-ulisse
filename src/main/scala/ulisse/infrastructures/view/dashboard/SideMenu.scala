package ulisse.infrastructures.view.dashboard

import net.miginfocom.swing.MigLayout
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel

import java.awt.Color
import javax.swing.BoxLayout
import scala.swing.event.MouseClicked
import scala.swing.{AbstractButton, BoxPanel, ButtonGroup, Component, Orientation, Panel, Reactor, ToggleButton}

trait SideMenu extends Component
object SideMenu:
  def apply(): SideMenu = SideMenuImpl()
  private case class SideMenuImpl() extends BoxPanel(Orientation.Vertical) with SideMenu with Reactor:
    private val buttonGroup = new ButtonGroup()

    private def configure[C <: Component](component: C): AbstractButton =
      component.fixedSize(50, 50).opaque(false).makeSelectable().addToGroup(buttonGroup)
    private def position[C <: Component](component: C): Unit =
      peer.add(component.peer)

    private val imagePanels = List(
      ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK).genericClickReaction(() => println("map")),
      ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK),
      ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK)
    ).map(configure)

    imagePanels.foreach(position)
