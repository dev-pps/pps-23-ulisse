package ulisse.infrastructures.view.menu

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentMixins.UpdatableContainer
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.{ComposedSwing, JStyles}
import ulisse.infrastructures.view.dashboard.Dashboard

import java.awt.Color
import scala.swing.*

final case class Menu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  private val card =
    JImageCard.vertical(
      ImagePanel.createSVGPanel("icons/add.svg", Color.BLACK),
      Label("new").centerHorizontally()
    )
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
