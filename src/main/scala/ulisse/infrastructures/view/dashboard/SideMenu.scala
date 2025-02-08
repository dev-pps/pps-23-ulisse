package ulisse.infrastructures.view.dashboard

import ulisse.infrastructures.view.StationSettings
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentConfigurations.{
  Alignment,
  ComponentConfiguration,
  ComponentWithConfiguration
}
import ulisse.infrastructures.view.components.ComponentMixins.UpdatableContainer
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.Images.ImagePanel
import ulisse.infrastructures.view.components.Styles
import ulisse.infrastructures.view.components.Selectables.SelectableGroup
import ulisse.infrastructures.view.map.EditorsView
import ulisse.infrastructures.view.simulation.SimulationPage

import java.awt.{Color, Insets}
import scala.swing.*

trait SideMenu extends Component
object SideMenu:
  def apply(uc: UpdatableContainer): SideMenu = SideMenuImpl(uc: UpdatableContainer)
  private case class SideMenuImpl(uc: UpdatableContainer) extends BoxPanel(Orientation.Vertical) with SideMenu:

    private val layout_bounds       = Insets(16, 16, 16, 16)
    private val header_spacing      = 24
    private val menu_item_spacing   = 24
    private val menu_item_height    = 50
    private val logo_size           = 50
    private val openCloseButtonSize = 25
//    private val menuCardStyle = JStyle.rectPaletteStyler(
//      JStyle.rect(JStyle.defaultSizeRect, JStyle.Dimension2D(5, 5), 25),
//      JStyle.palette(JStyle.transparentColor, Theme.light.forwardClick.withAlpha(150), Theme.light.forwardClick)
//      )

    private val header      = buildHeader()
    private val defaultPage = SimulationPage()
    private val menuCards: List[SelectableJImageCard] = List(
      buildMenuCard("icons/simulation.svg", "Simulation").genericClickReaction(() =>
        uc.update(defaultPage)
      ),
      buildMenuCard("icons/map.svg", "Editors").genericClickReaction(() => uc.update(EditorsView())),
      buildMenuCard("icons/train.svg", "Trains").genericClickReaction(() =>
        uc.update(StationSettings().stationEditorView)
      ),
      buildMenuCard("icons/settings.svg", "Settings")
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
      updateSize()
      uc.update(defaultPage)

    private def buildHeader(): JImageCard =
      val headerLeftContent = JImageCard.horizontal(
        ImagePanel.createImagePanel("icons/logo_circular.png").fixedSize(logo_size, logo_size),
        Label("Ulisse").alignLeft()
//        JStyle.transparent.copy(rect = JStyle.defaultRect.copy(padding = JStyle.createPadding(10, 10)))
      )
      val headerRightContent =
        ImagePanel.createSVGPanel("icons/keyboard_double_arrow_left.svg", Color.BLACK).fixedSize(
          openCloseButtonSize,
          openCloseButtonSize
        ).genericClickReaction(menuCallback)

      JImageCard.horizontalWithConfiguration(
        ComponentWithConfiguration(headerRightContent, ComponentConfiguration(Alignment.Center)),
        headerLeftContent
      ).reverse()

    private def buildMenuCard(imagePath: String, label: String): SelectableJImageCard =
      SelectableJImageCard.horizontal(
        ImagePanel.createSVGPanel(imagePath, Color.BLACK).fixedSize(menu_item_height, menu_item_height),
        Label(label).alignLeft()
      )

    private def updateSize(): Unit =
      val maxContentWidth = menuContent.foldLeft(0)((m, c) => math.max(m, c.realPreferredSize().width))
      menuContent.foreach: content =>
        content.fixedSize(maxContentWidth, menu_item_height)
      preferredSize = new Dimension(layout_bounds.right + layout_bounds.left + maxContentWidth, this.peer.getHeight)
      this.peer.setBounds(0, 0, preferredSize.width, preferredSize.height)
      revalidate()
      repaint()

    background = Theme.light.element
    build_layout()

    extension (imageCard: JImageCard)
      private def realPreferredSize(): Dimension =
        new Dimension(
          menu_item_height + (if imageCard.content.visible then imageCard.content.preferredSize.width
                              else 0) + 2 * imageCard.rect.padding.a,
          menu_item_height + 2 * imageCard.rect.padding.b
        )
      private def toggleLabel(): Unit = imageCard.content.visible = !imageCard.content.visible
