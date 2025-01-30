package ulisse.infrastructures.view.components

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter

import java.awt.Color
import javax.swing.border.LineBorder
import scala.swing.{BoxPanel, Component, Graphics2D, Label, Orientation, Panel, Swing, UIElement}

object Cards:
  trait Card extends Component

  object Card:
    def createSVGCard(svg: String, text: Option[String], color: Color): Card               = SVGCard(svg, color, text)
    def createDrawnCard(icon: (UIElement, Graphics2D) => Unit, text: Option[String]): Card = DrawnCard(icon, text)

  private def buildDescriptionContainer(text: Option[String]): Option[BoxPanel] =
    for t <- text yield new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += new Label(t)
      contents += Swing.HGlue

  private final case class SVGCard(svg: String, color: Color, text: Option[String])
      extends BoxPanel(Orientation.Vertical) with Card:
    border = new LineBorder(color, 2)
    contents += new Panel:
      val rawIcon = new FlatSVGIcon(svg)
      rawIcon.setColorFilter(ColorFilter(color => Color.GREEN))
      override def paint(g: Graphics2D): Unit =
        val icon = rawIcon.derive(size.width / 2, size.height / 2)
        icon.paintIcon(peer, g, (size.width - icon.getWidth) / 2, (size.height - icon.getHeight) / 2)
    for dc <- buildDescriptionContainer(text) do contents += dc

  private final case class DrawnCard(icon: (UIElement, Graphics2D) => Unit, text: Option[String])
      extends BoxPanel(Orientation.Vertical) with Card:
    border = new LineBorder(Color.BLACK, 2)
    contents += new Panel:
      override def paint(g: Graphics2D): Unit = icon(this, g)
    for dc <- buildDescriptionContainer(text) do contents += dc

  object Example:
    import Cards.*
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

    val svgCardExample: Card   = Cards.Card.createSVGCard("icon/mapIcon.svg", Some("Map"), Color.RED)
    val drawnCardExample: Card = Cards.Card.createDrawnCard(drawCross(20, 2, Color.BLUE), Some("Cross"))
