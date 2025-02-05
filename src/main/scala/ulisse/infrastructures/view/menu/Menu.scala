package ulisse.infrastructures.view.menu

import ulisse.infrastructures.view.UpdatableContainer
import ulisse.infrastructures.view.common.Theme
import ulisse.infrastructures.view.components.Cards.*
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
    JImageCard.vertical(ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK), Label("new").centerHorizontally())
      .fixedSize(100, 100)
      .genericClickReaction(() => root.update(Dashboard(root)))
      .styler(
        JStyler.styler(
          JStyler.roundRect(25),
          JStyler.palette(Theme.light.element, Theme.light.hover, Theme.light.forwardClick),
          JStyler.defaultFont,
          JStyler.border(Color.DARK_GRAY, 1)
        )
      )
  private val centerPanel = new BoxPanel(Orientation.Vertical):
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += card
      contents += Swing.HGlue
    contents += Swing.VGlue
  layout(centerPanel) = BorderPanel.Position.Center
