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
//  private val card = Card.createImageCard(ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK), "new")
//    .fixedSize(100, 100)
//    .defaultBorder()
//    .genericClickReaction(() => root.update(Dashboard(root)))

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
    opaque = false

  private val card = MenuCard(ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK), "new")
    .fixedSize(100, 100)
    .defaultBorder()
    .genericClickReaction(() => root.update(Dashboard(root)))

  private val centerPanel = new BoxPanel(Orientation.Vertical):
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += card
      contents += Swing.HGlue
    contents += Swing.VGlue
  layout(centerPanel) = BorderPanel.Position.Center
