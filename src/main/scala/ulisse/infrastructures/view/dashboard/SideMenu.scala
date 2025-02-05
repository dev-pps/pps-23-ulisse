package ulisse.infrastructures.view.dashboard

import net.miginfocom.swing.MigLayout
import ulisse.infrastructures.view.common.Theme
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.{JComponent, JPanel, JStyler, SelectableGroup}

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

////    private def configure[C <: Component](component: C): Component =
////      component.opaque(true)//.makeSelectable().addToGroup(buttonGroup)

    contents += Swing.VStrut(10)
    private def position[C <: Component](component: C): Unit =
      contents += component
      contents += Swing.VStrut(10)

    val menuCallback: () => Unit = () => {
      menuContent.foreach(_.toggleLabel())
      header.image.rotation = header.image.rotation + 180
      revalidate()
      updateSize()
    }

    private val menuCardStyle = JStyler.rectPaletteStyler(
      JStyler.roundRect(10),
      JStyler.palette(Theme.light.element, Theme.light.hover, Theme.light.forwardClick)
    )

    def updateSize(): Unit =
      val maxContentWidth = menuContent.foldLeft(0)((m, c) => math.max(m, c.realPreferredSize().width))

      menuContent.foreach: content =>
        content.fixedSize(maxContentWidth, content.preferredSize.height)

      this.peer.setPreferredSize(new Dimension(20 + maxContentWidth, this.peer.getHeight))
      this.peer.setBounds(0, 0, 20 + maxContentWidth, this.peer.getHeight)
      revalidate()
      repaint()

    private val headerLeftContent = ImageCard.horizontal(
      ImagePanel.createImagePanel("icon/logo_circular.png").fixedSize(50, 50),
      Label("Ulisse").alignLeft()
    )
    private val headerRightContent =
      ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK).fixedSize(25, 25).styler(
        menuCardStyle.copy(palette = menuCardStyle.palette.copy(background = JStyler.transparentColor))
      ).genericClickReaction(menuCallback)

    private val header = ImageCard.horizontalWithConfiguration(
      ComponentWithConfiguration(headerRightContent, ComponentConfiguration(Alignment.Center)),
      headerLeftContent
    ).reverse()

    private val menuCards: List[JImageCard] = List[JImageCard](
      JImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/simulation.svg", Color.BLACK).fixedSize(50, 50),
        Label("Simulation").alignLeft(),
        menuCardStyle
      ),
      JImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/map.svg", Color.BLACK).fixedSize(50, 50),
        Label("Editors").alignLeft(),
        menuCardStyle
      ),
      JImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/train.svg", Color.BLACK).fixedSize(50, 50),
        Label("Trains").alignLeft(),
        menuCardStyle
      ),
      JImageCard.horizontal(
        ImagePanel.createSVGPanel("icon/settings.svg", Color.BLACK).fixedSize(50, 50),
        Label("Settings").alignLeft(),
        menuCardStyle
      )
    )

    private val buttonGroup: SelectableGroup = SelectableGroup(menuCards*)
    private val menuContent                  = header +: menuCards

    position(header)
    private val (topMenu, bottomMenu) = menuCards.splitAt(3)
    topMenu.foreach(position)
    position(Swing.VGlue)
    bottomMenu.foreach(position)

    updateSize()
extension (imageCard: ImageCard)
  def realPreferredSize(): Dimension =
    new Dimension(50 + (if imageCard.content.visible then imageCard.content.preferredSize.width else 0), 50)
  def toggleLabel(): Unit = imageCard.content.visible = !imageCard.content.visible
