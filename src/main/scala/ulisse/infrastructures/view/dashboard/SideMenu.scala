package ulisse.infrastructures.view.dashboard

import net.miginfocom.swing.MigLayout
import ulisse.infrastructures.view.common.Theme
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.{JComponent, JStyler}

import java.awt.{Color, ComponentOrientation}
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

//    private def configure[C <: Component](component: C): Component =
//      component.opaque(true)//.makeSelectable().addToGroup(buttonGroup)

    private def position[C <: Component](component: C): Unit =
      contents += component

//    val menuCallback = () => {
//      menuCards.foreach(_.toggleLabel())
//      updateSize()
//      revalidate()
//      repaint()
//    }
//
    def updateSize(): Unit =
      val maxCardWidth = menuCards.foldLeft(0)((m, c) => math.max(m, c.realPreferredSize().width))
      println(s"ua $maxCardWidth")
      menuCards.foreach: card =>
        card.fixedSize(maxCardWidth, card.preferredSize.height)
      SideMenuImpl.this.preferredSize = new Dimension(maxCardWidth, 400)
      SideMenuImpl.this.peer.setBounds(0, 0, maxCardWidth, SideMenuImpl.this.peer.getHeight)

    private val menuCards: List[MenuCard] = List(
//      HeaderCard(ImagePanel.createSVGPanel("icon/keyboard_double_arrow_right.svg", Color.BLACK), "Dashboard").visible(
//        false
//      ).genericClickReaction(menuCallback),
//      HeaderCard(
//        ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK),
//        "Infrastructure"
//      ).genericClickReaction(menuCallback),
      MenuCard(ImagePanel.createSVGPanel("icon/simulation.svg", Color.BLACK), "Simulation"),
      MenuCard(ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK), "Editors"),
      MenuCard(ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK), "Trains"),
      MenuCard(ImagePanel.createSVGPanel("icon/settings.svg", Color.BLACK), "Settings")
    )

    updateSize()

    private val (header, content) = menuCards.splitAt(2)
//    header.foreach(position)
//    content.map(configure)

    private val (topMenu, bottomMenu) = content.splitAt(3)
    topMenu.foreach(position)
    position(Swing.VGlue)
    bottomMenu.foreach(position)

trait TC extends Component:
  val imagePanel: ImagePanel
  val textLabel: Label
  def realPreferredSize(): Dimension =
    new Dimension(50 + (if textLabel.visible then textLabel.preferredSize.width else 0), 50)
  def toggleLabel(): Unit = textLabel.visible = !textLabel.visible

final case class HeaderCard(imagePanel: ImagePanel, text: String) extends BoxPanel(Orientation.Horizontal) with TC:
  val textLabel = new Label(text)
  contents += textLabel
  contents += Swing.HGlue
  contents += imagePanel.fixedSize(50, 50)

final case class MenuCard(imagePanel: ImagePanel, text: String) extends BoxPanel(Orientation.Horizontal)
    with JComponent(
      JStyler.rectPaletteStyler(
        JStyler.roundRect(10),
        JStyler.palette(Theme.light.element, Theme.light.hover, Theme.light.backwardClick)
      )
    ):
  val textLabel = new Label(text)
  contents += imagePanel.fixedSize(50, 50)
  contents += textLabel
  contents += Swing.HGlue
  def realPreferredSize(): Dimension =
    new Dimension(50 + (if textLabel.visible then textLabel.preferredSize.width else 0), 50)

  def toggleLabel(): Unit = textLabel.visible = !textLabel.visible
