package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.ComponentUtils.*

import java.awt.{Color, ComponentOrientation}
import javax.imageio.ImageIO
import scala.swing.{
  BoxPanel,
  Component,
  Graphics2D,
  Label,
  Orientation,
  Panel,
  RadioButton,
  SequentialContainer,
  Swing,
  UIElement
}

object Cards:
  trait ImageCard extends Component with SequentialContainer.Wrapper:
    val image: ImagePanel
    val content: Component
    def reverse(): ImageCard =
      @SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
      val reversedContent = this.contents.reverse
      this.contents.clear()
      this.contents ++= reversedContent
      this

  trait JImageCard extends ImageCard with JComponent

  object ImageCard:
    def horizontal(image: ImagePanel, component: Component): ImageCard =
      ImageCardImpl(image, component, Orientation.Horizontal)
    def vertical(image: ImagePanel, component: Component): ImageCard =
      ImageCardImpl(image, component, Orientation.Vertical)

  object JImageCard:
    def horizontal(image: ImagePanel, component: Component): JImageCard =
      JImageCardImpl(image, component, Orientation.Horizontal)
    def vertical(image: ImagePanel, component: Component): JImageCard =
      JImageCardImpl(image, component, Orientation.Vertical)

  private final case class ImageCardImpl(image: ImagePanel, content: Component, orientation: Orientation.Value)
      extends BoxPanel(orientation) with ImageCard:
    opaque = false
    contents += image.center(); contents += content

  private final case class JImageCardImpl(image: ImagePanel, content: Component, orientation: Orientation.Value)
      extends BoxPanel(orientation) with JImageCard with JComponent(JStyler.defaultStyler):
    listenTo(image.mouse.clicks, image.mouse.moves, content.mouse.clicks, content.mouse.moves)
    contents += image; contents += content

  object Example:
    val imageCardExample: ImageCard =
      Cards.JImageCard.vertical(ImagePanels.Example.imagePanelExample, Label("Logo").centerHorizontally())
    val svgCardExample: ImageCard =
      Cards.JImageCard.horizontal(ImagePanels.Example.svgPanelExample, Label("Map").centerHorizontally())
    val drawnCardExample: ImageCard =
      Cards.JImageCard.horizontal(ImagePanels.Example.drawnPanelExample, Label("Cross").centerHorizontally())
