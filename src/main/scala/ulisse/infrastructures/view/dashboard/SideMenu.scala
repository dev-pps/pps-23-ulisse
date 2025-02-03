package ulisse.infrastructures.view.dashboard

import net.miginfocom.swing.MigLayout
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel

import java.awt.Color
import scala.swing.event.MouseClicked
import scala.swing.{AbstractButton, ButtonGroup, Component, Panel, Reactor, ToggleButton}

trait SideMenu extends Component
object SideMenu:
  def apply(): SideMenu = SideMenuImpl()
  private case class SideMenuImpl() extends Panel with SideMenu with Reactor:
    private val buttonGroup = new ButtonGroup()

    private def configure[C <: Component](component: C): AbstractButton =
      component.opaque(false).fListenTo(mouse.clicks).makeSelectable().addToGroup(buttonGroup)
    private def select(c: Component): Unit =
      println("Selected")
    private def position[C <: Component](component: C): Unit = peer.add(component.peer, "h 50!, w 50!, wrap")

    reactions += {
      case MouseClicked(c: Component, _, _, _, _) => select(c)
    }

    peer.setLayout(new MigLayout("center, debug, insets 5 5 0 0", "[]", "[]10[]"))
    List(
      ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK),
      ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK),
      ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK)
    ).foreach(configure andThen position)
