package ulisse.infrastructures.view.dashboard

import ulisse.infrastructures.view.{StationSettings, UpdatableContainer}
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.JStyler
import ulisse.infrastructures.view.components.Selectables.SelectableGroup
import ulisse.infrastructures.view.map.{EditorsView, MapPanel}
import ulisse.infrastructures.view.menu.Menu

import java.awt.{Color, Insets}
import scala.swing.{BoxPanel, Component, Dimension, Label, Orientation, Reactor, Swing}

trait SideMenu extends Component
object SideMenu:
  def apply(uc: UpdatableContainer): SideMenu = SideMenuImpl(uc: UpdatableContainer)
  private case class SideMenuImpl(uc: UpdatableContainer) extends BoxPanel(Orientation.Vertical) with SideMenu
      with Reactor:

    private val layout_bounds     = Insets(16, 16, 16, 16)
    private val header_spacing    = 24
    private val menu_item_spacing = 24
    private val menu_item_height  = 50
    private val menuCardStyle = JStyler.rectPaletteStyler(
      JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(5, 5), 25),
      JStyler.palette(Theme.light.forwardClick.withAlpha(150), Theme.light.overlayElement, Theme.light.forwardClick)
    )

    private val header = buildHeader()

    private val menuCards: List[JImageCard] = List(
      buildMenuCard("icon/simulation.svg", "Simulation").genericClickReaction(() =>
        uc.update(StationSettings().stationEditorView)
      ),
      buildMenuCard("icon/map.svg", "Editors").genericClickReaction(() => uc.update(Menu(uc))),
      buildMenuCard("icon/train.svg", "Trains").genericClickReaction(() => uc.update(EditorsView())),
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
      uc.update(StationSettings().stationEditorView)
      updateSize()

    private def buildHeader(): JImageCard =
      val headerLeftContent = JImageCard.horizontal(
        ImagePanel.createImagePanel("icon/logo_circular.png").fixedSize(50, 50),
        Label("Ulisse").alignLeft(),
        JStyler.transparent.copy(rect = JStyler.defaultRect.copy(padding = JStyler.createPadding(10, 10)))
      )
      val headerRightContent =
        ImagePanel.createSVGPanel("icon/keyboard_double_arrow_left.svg", Color.BLACK).fixedSize(
          25,
          25
        ).styler(
          menuCardStyle.copy(palette = menuCardStyle.palette.copy(background = JStyler.transparentColor))
        ).genericClickReaction(menuCallback)

      JImageCard.horizontalWithConfiguration(
        ComponentWithConfiguration(headerRightContent, ComponentConfiguration(Alignment.Center)),
        headerLeftContent,
        JStyler.transparent
      ).reverse()

    private def buildMenuCard(imagePath: String, label: String): JImageCard =
      JImageCard.horizontal(
        ImagePanel.createSVGPanel(imagePath, Color.BLACK).fixedSize(menu_item_height, menu_item_height),
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
    new Dimension(
      50 + (if imageCard.content.visible then imageCard.content.preferredSize.width
            else 0) + 2 * imageCard.getStyler.rect.padding.width,
      50 + 2 * imageCard.getStyler.rect.padding.height
    )
  def toggleLabel(): Unit = imageCard.content.visible = !imageCard.content.visible
