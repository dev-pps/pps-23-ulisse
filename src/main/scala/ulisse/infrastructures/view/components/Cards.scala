package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel

import java.awt.Color
import javax.imageio.ImageIO
import scala.swing.{BoxPanel, Component, Graphics2D, Label, Orientation, Panel, RadioButton, Swing, UIElement}

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
object Cards:
  trait Card extends Component

  object Card:
    def createImageCard(image: ImagePanel, text: String): Card = ImageCard(image, text)

  private final case class ImageCard(image: ImagePanel, text: String)
      extends BoxPanel(Orientation.Vertical) with Card:
    contents += image
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += new Label(text)
      contents += Swing.HGlue

  object Example:
    val imageCardExample: Card = Cards.Card.createImageCard(ImagePanels.Example.imagePanelExample, "Logo")
    val svgCardExample: Card   = Cards.Card.createImageCard(ImagePanels.Example.svgPanelExample, "Map")
    val drawnCardExample: Card = Cards.Card.createImageCard(ImagePanels.Example.drawnPanelExample, "Cross")
