package ulisse.infrastructures.view.menu

import ulisse.infrastructures.view.common.Themes.Theme
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentMixins.UpdatableContainer
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.JComponent
import ulisse.infrastructures.view.components.JStyler
import ulisse.infrastructures.view.dashboard.Dashboard

import java.awt.Color
import scala.swing.{BorderPanel, BoxPanel, Dimension, Label, Orientation, Swing}

final case class Menu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  private val card =
    JImageCard.vertical(
      ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK),
      Label("new").centerHorizontally(),
      JStyler.default
    )
      .fixedSize(100, 100)
      .genericClickReaction(() => root.update(Dashboard(root)))
      .styler(
        JStyler.styler(
          JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(0, 0), 25),
          JStyler.palette(Theme.light.element, Theme.light.overlayElement, Theme.light.forwardClick),
          JStyler.defaultFont,
          JStyler.border(Color.DARK_GRAY, 1)
        )
      )

  layout(card.center()) = BorderPanel.Position.Center
