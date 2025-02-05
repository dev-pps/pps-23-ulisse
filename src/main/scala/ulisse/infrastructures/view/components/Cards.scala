package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ComponentMixins.Selectable

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
  trait ImageCard extends Component with SequentialContainer.Wrapper with Selectable:
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
      ImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Horizontal)
    def vertical(image: ImagePanel, component: Component): ImageCard =
      ImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Vertical)
    def horizontalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): ImageCard =
      ImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Horizontal
      )
    def verticalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): ImageCard =
      ImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Vertical
      )

  object JImageCard:
    def horizontal(image: ImagePanel, component: Component): JImageCard =
      JImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Horizontal)

    def vertical(image: ImagePanel, component: Component): JImageCard =
      JImageCardImpl(image, ComponentConfiguration.empty(), component, Orientation.Vertical)

    def horizontalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): JImageCard =
      JImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Horizontal
      )

    def verticalWithConfiguration(
        imageWithConfiguration: ComponentWithConfiguration[ImagePanel],
        component: Component
    ): JImageCard =
      JImageCardImpl(
        imageWithConfiguration.component,
        imageWithConfiguration.configuration,
        component,
        Orientation.Vertical
      )

  private final case class ImageCardImpl(
      image: ImagePanel,
      imageConfiguration: ComponentConfiguration,
      content: Component,
      orientation: Orientation.Value
  ) extends BoxPanel(orientation) with ImageCard:
    opaque = false
    contents += image.align(imageConfiguration.alignment); contents += content

  private final case class JImageCardImpl(
      image: ImagePanel,
      imageConfiguration: ComponentConfiguration,
      content: Component,
      orientation: Orientation.Value
  ) extends BoxPanel(orientation) with JImageCard with JComponent(JStyler.defaultStyler):
    private val styler = getStyler
    listenTo(image.mouse.clicks, image.mouse.moves, content.mouse.clicks, content.mouse.moves)
    contents += image.align(imageConfiguration.alignment); contents += content
    override def selected_=(newSelected: Boolean): Unit =
      if newSelected then setStyler(styler)
      else setStyler(styler.copy(palette = styler.palette.copy(background = JStyler.transparentColor)))
      super.selected_=(newSelected)

  object Example:
    val imageCardExample: ImageCard =
      Cards.JImageCard.vertical(ImagePanels.Example.imagePanelExample, Label("Logo").centerHorizontally())
    val svgCardExample: ImageCard =
      Cards.JImageCard.horizontal(ImagePanels.Example.svgPanelExample, Label("Map").centerHorizontally())
    val drawnCardExample: ImageCard =
      Cards.JImageCard.horizontal(ImagePanels.Example.drawnPanelExample, Label("Cross").centerHorizontally())
