package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.ComponentUtils.*
import java.awt.{Color, ComponentOrientation}
import javax.imageio.ImageIO
import scala.swing.{BoxPanel, Component, Graphics2D, Label, Orientation, Panel, RadioButton, Swing, UIElement}

object Cards:
  trait ImageCard extends JComponent:
    val image: ImagePanel
    val content: Component

  object ImageCard:
    def horizontal(image: ImagePanel, component: Component): ImageCard =
      ImageCardImpl(image, component, Orientation.Horizontal)
    def vertical(image: ImagePanel, component: Component): ImageCard =
      ImageCardImpl(image, component, Orientation.Vertical)

  private final case class ImageCardImpl(image: ImagePanel, content: Component, orientation: Orientation.Value)
      extends BoxPanel(orientation) with ImageCard with JComponent(JStyler.defaultStyler):
    listenTo(image.mouse.clicks, image.mouse.moves, content.mouse.clicks, content.mouse.moves)
    contents += image; contents += content

  object Example:
    val imageCardExample: ImageCard =
      Cards.ImageCard.vertical(ImagePanels.Example.imagePanelExample, Label("Logo").centerHorizontally())
    val svgCardExample: ImageCard =
      Cards.ImageCard.horizontal(ImagePanels.Example.svgPanelExample, Label("Map").centerHorizontally())
    val drawnCardExample: ImageCard =
      Cards.ImageCard.horizontal(ImagePanels.Example.drawnPanelExample, Label("Cross").centerHorizontally())
