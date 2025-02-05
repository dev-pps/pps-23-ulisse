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
  BorderPanel,
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

////    private def configure[C <: Component](component: C): Component =
////      component.opaque(true)//.makeSelectable().addToGroup(buttonGroup)
//
//    contents += Swing.HStrut(10)
//    contents += menu
//    contents += Swing.HStrut(10)
    contents += Swing.VStrut(10)
    private def position[C <: Component](component: C): Unit =
      contents += component
      contents += Swing.VStrut(10)

    val menuCallback = () => {
      menuCards.foreach(_.toggleLabel())
      revalidate()
      updateSize()
    }

    private val menuCardStyle = JStyler.rectPaletteStyler(
      JStyler.roundRect(10),
      JStyler.palette(Theme.light.element, Theme.light.hover, Theme.light.forwardClick)
    )

    def updateSize(): Unit =
      val maxCardWidth = menuCards.foldLeft(0)((m, c) => math.max(m, c.realPreferredSize().width))
      menuCards.foreach: card =>
        card.fixedSize(maxCardWidth, card.preferredSize.height)
      this.peer.setPreferredSize(new Dimension(20 + maxCardWidth, this.peer.getHeight))
      this.peer.setBounds(0, 0, 20 + maxCardWidth, this.peer.getHeight)
      revalidate()
      repaint()

    private val headerContent =
      ImageCard.horizontal(ImagePanel.createImagePanel("icon/logo.jpg").fixedSize(50, 50), Label("Ulisse").alignLeft())
    private val menuCards: List[ImageCard] = List(
      ImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK).fixedSize(25, 25),
        headerContent
      ).genericClickReaction(menuCallback),
      ImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/simulation.svg", Color.BLACK).fixedSize(50, 50),
        Label("Simulation").alignLeft()
      ),
      ImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK).fixedSize(50, 50),
        Label("Editors").alignLeft()
      ),
      ImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK).fixedSize(50, 50),
        Label("Trains").alignLeft()
      ),
      ImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/settings.svg", Color.BLACK).fixedSize(50, 50),
        Label("Settings").alignLeft()
      )
    ).map(_.styler(menuCardStyle))

    private val (header, content) = menuCards.splitAt(1)
    header.foreach(position)
//    content.map(configure)

    private val (topMenu, bottomMenu) = content.splitAt(3)
    topMenu.foreach(position)
    position(Swing.VGlue)
    bottomMenu.foreach(position)

    updateSize()
extension (imageCard: ImageCard)
  def realPreferredSize(): Dimension =
    new Dimension(50 + (if imageCard.content.visible then imageCard.content.preferredSize.width else 0), 50)
  def toggleLabel(): Unit = imageCard.content.visible = !imageCard.content.visible
