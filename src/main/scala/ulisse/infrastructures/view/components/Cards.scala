package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter

import java.awt.Color
import javax.imageio.ImageIO
import scala.swing.{BoxPanel, Component, Graphics2D, Label, Orientation, Panel, RadioButton, Swing, UIElement}

@SuppressWarnings(Array("org.wartremover.warts.MutableDataStructures"))
object Cards:
  trait Card extends Component

  object Card:
    def createImageCard(image: String, text: Option[String]): Card                         = ImageCard(image, text)
    def createSVGCard(svg: String, text: Option[String], color: Color): Card               = SVGCard(svg, color, text)
    def createDrawnCard(icon: (UIElement, Graphics2D) => Unit, text: Option[String]): Card = DrawnCard(icon, text)

  private def buildDescriptionContainer(text: Option[String]): Option[BoxPanel] =
    val bx =
      for t <- text yield new BoxPanel(Orientation.Horizontal):
        contents += Swing.HGlue
        contents += new Label(t)
        contents += Swing.HGlue
    bx

  private final case class ImageCard(imageUrl: String, text: Option[String])
      extends BoxPanel(Orientation.Vertical) with Card:
    private val image = ImageIO.read(ClassLoader.getSystemResource(imageUrl))
    contents += new Panel:
      override def paint(g: Graphics2D): Unit =
        val size = math.min(peer.getWidth, peer.getHeight) / 2
        g.drawImage(image, (peer.getWidth - size) / 2, (peer.getHeight - size) / 2, size, size, peer)
    for dc <- buildDescriptionContainer(text) do contents += dc

  private final case class SVGCard(svg: String, color: Color, text: Option[String])
      extends BoxPanel(Orientation.Vertical) with Card:
    contents += new Panel:
      val rawIcon = new FlatSVGIcon(svg)
      rawIcon.setColorFilter(ColorFilter(_ => color))
      override def paint(g: Graphics2D): Unit =
        val size = math.min(peer.getWidth, peer.getHeight) / 2
        val icon = rawIcon.derive(size, size)
        icon.paintIcon(peer, g, (peer.getWidth - icon.getWidth) / 2, (peer.getHeight - icon.getHeight) / 2)
    for dc <- buildDescriptionContainer(text) do contents += dc

  private final case class DrawnCard(icon: (UIElement, Graphics2D) => Unit, text: Option[String])
      extends BoxPanel(Orientation.Vertical) with Card:
    contents += new Panel:
      override def paint(g: Graphics2D): Unit = icon(this, g)
    for dc <- buildDescriptionContainer(text) do contents += dc

  object Example:
    def drawCross(preferredLength: Int, preferredThickness: Int, color: Color)(
        element: UIElement,
        g: Graphics2D
    ): Unit =
      val oldColor = g.getColor
      g.setColor(color)
      val center    = (element.size.width / 2, element.size.height / 2)
      val length    = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredLength)
      val thickness = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredThickness)
      g.fillRect(center._1 - thickness / 2, center._2 - length / 2, thickness, length)
      g.fillRect(center._1 - length / 2, center._2 - thickness / 2, length, thickness)
      g.setColor(oldColor)

    val imageCardExample: Card = Cards.Card.createImageCard("icon/logo.jpg", Some("Logo"))
    val svgCardExample: Card   = Cards.Card.createSVGCard("icon/map.svg", Some("Map"), Color.RED)
    val drawnCardExample: Card = Cards.Card.createDrawnCard(drawCross(20, 2, Color.BLUE), Some("Cross"))
