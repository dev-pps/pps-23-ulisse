package ulisse.infrastructures.view.menu

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentMixins.UpdatableContainer
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ui.ExtendedSwing.PicturePanel
import ulisse.infrastructures.view.components.ui.composed.ComposedSwing
import ulisse.infrastructures.view.components.ui.decorators.Styles
import ulisse.infrastructures.view.dashboard.Dashboard
import ulisse.infrastructures.view.simulation.SimulationPage.SimulationPageControlPanel

import java.awt.Color
import scala.swing.*

final case class Menu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  val picture = PicturePanel()
  picture.picture = "icons/add.svg"

  private val card =
    JImageCard.vertical(picture, Label("new").centerHorizontally())
      .fixedSize(100, 100)
      .genericClickReaction(() => root.update(Dashboard(root))
//      .styler(
//        JStyle.styler(
//          JStyle.rect(JStyle.defaultSizeRect, JStyle.Dimension2D(0, 0), 25),
//          JStyle.palette(Theme.light.element, Theme.light.overlayElement, Theme.light.forwardClick),
//          JStyle.defaultFont,
//          JStyle.border(Color.DARK_GRAY, 1)
//        )
      )

  layout(card.center()) = BorderPanel.Position.Center
