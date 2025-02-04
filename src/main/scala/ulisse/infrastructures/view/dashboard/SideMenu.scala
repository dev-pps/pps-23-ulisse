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
      contents += component

    val openMenu: ImagePanel = ImagePanel.createSVGPanel("icon/keyboard_double_arrow_right.svg", Color.BLACK)
      .genericClickReaction(() =>
        SideMenuImpl.this.preferredSize = new Dimension(200, 400)
        openMenu.visible = false
        closeMenu.visible = true
      )
    val closeMenu: AbstractButton =
      configure(ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK)
        .genericClickReaction(() =>
          SideMenuImpl.this.preferredSize = new Dimension(58, 400)
          openMenu.visible = true
          closeMenu.visible = false
        ))
    closeMenu.visible = false

    private val menuCards = List(
      MenuCard(ImagePanel.createSVGPanel("icon/simulation.svg", Color.BLACK), "Simulation"),
      MenuCard(ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK), "Editors"),
      MenuCard(ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK), "Trains"),
      MenuCard(ImagePanel.createSVGPanel("icon/settings.svg", Color.BLACK), "Settings")
    ).map(configure)

    private val maxCardWidth = menuCards.foldLeft(0)((m, c) => math.max(m,c.preferredSize.width))
    menuCards.foreach:
      card => card.fixedSize(maxCardWidth, card.preferredSize.height)

    position(openMenu)
    private val (topMenu, bottomMenu) = menuCards.splitAt(3)
    topMenu.foreach(position)
    position(Swing.VGlue)
    bottomMenu.foreach(position)

final case class MenuCard(imagePanel: ImagePanel, text: String) extends BoxPanel(Orientation.Horizontal):
  contents += imagePanel.fixedSize(50, 50)
  contents += new Label(text)
  contents += Swing.HGlue
  def toggleLabel(): Unit = peer.getComponent(1).setVisible(!peer.getComponent(1).isVisible)
