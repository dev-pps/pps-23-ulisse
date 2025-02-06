package ulisse.infrastructures.view.dashboard

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.{JStyler, SelectableGroup}

import java.awt.{Color, Insets}
import scala.swing.{BoxPanel, Component, Dimension, Label, Orientation, Reactor, Swing}

trait SideMenu extends Component
object SideMenu:
  def apply(): SideMenu = SideMenuImpl()
  private case class SideMenuImpl() extends BoxPanel(Orientation.Vertical) with SideMenu with Reactor:

    private val layout_bounds        = Insets(16, 16, 16, 16)
    private val header_spacing       = 24
    private val menu_item_spacing    = 24
    private val menu_item_height     = 50
    private val menu_item_image_size = 16
    private val menuCardStyle = JStyler.rectPaletteStyler(
      JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(0, 0), 25),
      JStyler.palette(Theme.light.element, Theme.light.overlayElement, Theme.light.forwardClick)
    )

    private val header = buildHeader()

    private val menuCards: List[JImageCard] = List(
      buildMenuCard("icon/simulation.svg", "Simulation"),
      buildMenuCard("icon/map.svg", "Editors"),
      buildMenuCard("icon/train.svg", "Trains"),
      buildMenuCard("icon/settings.svg", "Settings")
    )

    private val buttonGroup: SelectableGroup = SelectableGroup(menuCards*)
    private val menuContent                  = header +: menuCards

    private def menuCallback(): Unit =
      menuContent.foreach(_.toggleLabel())
      header.image.rotation = header.image.rotation + 180
      revalidate()
      updateSize()

    private def build_layout(): Unit =
      def position[C <: Component](component: C): Unit =
        contents += component
        contents += Swing.VStrut(menu_item_spacing)
      println(s"Building layout $layout_bounds")
      contents += Swing.VStrut(layout_bounds.top)
      contents += header
      contents += Swing.VStrut(header_spacing)
      val (topMenu, other)       = menuCards.splitAt(3)
      val (bottomMenu, lastItem) = other.splitAt(other.length - 1)
      topMenu.foreach(position)
      position(Swing.VGlue)
      bottomMenu.foreach(position)
      lastItem.foreach(position)
      contents += Swing.VStrut(layout_bounds.bottom)
      updateSize()

    private def buildHeader(): JImageCard =
      val headerLeftContent = JImageCard.horizontal(
        ImagePanel.createImagePanel("icon/logo_circular.png").fixedSize(50, 50),
        Label("Ulisse").alignLeft(),
        JStyler.transparent
      )
      val headerRightContent =
        ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK).fixedSize(
          25,
          25
        ).genericClickReaction(menuCallback)

      JImageCard.horizontalWithConfiguration(
        ComponentWithConfiguration(headerRightContent, ComponentConfiguration(Alignment.Center)),
        headerLeftContent,
        menuCardStyle.copy(palette =
          JStyler.palette(JStyler.transparentColor, JStyler.transparentColor, JStyler.transparentColor)
        )
      ).reverse()

    private def buildMenuCard(imagePath: String, label: String): JImageCard =
      JImageCard.horizontal(
        ImagePanel.createSVGPanel(imagePath, Color.BLACK).fixedSize(menu_item_image_size, menu_item_image_size),
        Label(label).alignLeft(),
        menuCardStyle
      )

    private def updateSize(): Unit =
      val maxContentWidth = menuContent.foldLeft(0)((m, c) => math.max(m, c.realPreferredSize().width))
      menuContent.foreach: content =>
        content.fixedSize(maxContentWidth, menu_item_height)
      preferredSize = new Dimension(layout_bounds.right + layout_bounds.left + maxContentWidth, this.peer.getHeight)
      this.peer.setBounds(0, 0, preferredSize.width, preferredSize.height)
      revalidate()
      repaint()

    build_layout()

extension (imageCard: JImageCard)
  def realPreferredSize(): Dimension =
    new Dimension(50 + (if imageCard.content.visible then imageCard.content.preferredSize.width else 0), 50)
  def toggleLabel(): Unit = imageCard.content.visible = !imageCard.content.visible
