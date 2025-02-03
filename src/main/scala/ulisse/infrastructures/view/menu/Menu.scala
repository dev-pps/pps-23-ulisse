package ulisse.infrastructures.view.menu

import ulisse.infrastructures.view.UpdatableContainer
import ulisse.infrastructures.view.components.Cards.*
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.dashboard.Dashboard

import java.awt.Color
import scala.swing.{BorderPanel, BoxPanel, Dimension, Orientation, Swing}

final case class Menu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  private val card = Card.createImageCard(ImagePanel.createSVGPanel("icon/add.svg", Color.BLACK), "new")
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
