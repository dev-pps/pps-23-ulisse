package ulisse.infrastructures.view.dashboard

import net.miginfocom.swing.MigLayout
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel

import java.awt.Color
import javax.swing.BoxLayout
import scala.swing.event.MouseClicked
import scala.swing.{
  AbstractButton,
  BoxPanel,
  ButtonGroup,
  Component,
  Dimension,
  Label,
  Orientation,
  Panel,
  Reactor,
  Swing,
  ToggleButton
}

trait SideMenu extends Component
object SideMenu:
  def apply(): SideMenu = SideMenuImpl()
  private case class SideMenuImpl() extends BoxPanel(Orientation.Vertical) with SideMenu with Reactor:
    private val buttonGroup = new ButtonGroup()

    private def configure[C <: Component](component: C): AbstractButton =
      component.opaque(false).makeSelectable().addToGroup(buttonGroup)

    private def position[C <: Component](component: C): Unit =
      peer.add(component.peer)

    val openMenu: ImagePanel = ImagePanel.createSVGPanel("icon/keyboard_double_arrow_right.svg", Color.BLACK)
      .genericClickReaction(() =>
        SideMenuImpl.this.preferredSize = new Dimension(200, 400)
        peer.remove(openMenu.peer)
        peer.add(closeMenu.peer, 0)
        SideMenuImpl.this.revalidate()
        SideMenuImpl.this.repaint()
      )
    val closeMenu: AbstractButton =
      configure(ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK)
        .genericClickReaction(() =>
          SideMenuImpl.this.preferredSize = new Dimension(58, 400)
          peer.remove(closeMenu.peer)
          peer.add(openMenu.peer, 0)
          SideMenuImpl.this.revalidate()
          SideMenuImpl.this.repaint()
        ))
    private val imagePanels = List(
      openMenu,
      MenuCard(ImagePanel.createSVGPanel("icon/simulation.svg", Color.BLACK), "Simulation"),
      MenuCard(ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK), "Editors").genericClickReaction(() =>
        println("map")
      ),
      MenuCard(ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK), "Trains"),
      MenuCard(ImagePanel.createSVGPanel("icon/settings.svg", Color.BLACK), "Settings")
    ).map(configure)
    private val (topMenu, bottomMenu) = imagePanels.splitAt(4)
    topMenu.foreach(position)
    position(Swing.VGlue)
    bottomMenu.foreach(position)

final case class MenuCard(imagePanel: ImagePanel, text: String) extends BoxPanel(Orientation.Horizontal):
  imagePanel.fixedSize(50, 50)
  peer.add(imagePanel.peer)
  peer.add(new Label(text).peer)
  def toggleLabel(): Unit = peer.getComponent(1).setVisible(!peer.getComponent(1).isVisible)
