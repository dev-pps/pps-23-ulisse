package ulisse.infrastructures.view.menu

import ulisse.infrastructures.view.UpdatableContainer

import java.awt.{Color, Graphics}
import javax.swing.border.{EmptyBorder, LineBorder}
import scala.swing.{
  BorderPanel,
  BoxPanel,
  Dimension,
  FlowPanel,
  Graphics2D,
  Label,
  Orientation,
  Panel,
  Rectangle,
  Swing,
  UIElement
}

def drawCross(preferredLength: Int, preferredThickness: Int)(element: UIElement)(g: Graphics2D): Unit =
  val oldColor = g.getColor
  g.setColor(Color.ORANGE)
  val center = (element.size.width / 2, element.size.height / 2)
  println(center)
  val length    = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredLength)
  val thickness = math.min(math.min(element.size.width / 2, element.size.height / 2), preferredThickness)
  g.fillRect(center._1 - thickness / 2, center._2 - length / 2, thickness, length)
  g.fillRect(center._1 - length / 2, center._2 - thickness / 2, length, thickness)
  g.setColor(oldColor)

final case class Card(icon: UIElement => Graphics2D => Unit, text: String) extends BoxPanel(Orientation.Vertical):
  val configuredIcon = icon(this)
  val button = new Panel:
    override def paint(g: Graphics2D): Unit = configuredIcon(g)
  val description = new Label(text)
  val descriptionContainer = new BoxPanel(Orientation.Horizontal):
    contents += Swing.HGlue
    contents += description
    contents += Swing.HGlue
  border = new LineBorder(Color.BLACK, 2)
  contents += button
  contents += descriptionContainer
  listenTo(button.mouse.clicks)

final case class AppMenu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  val card = Card(drawCross(20, 2), "Cross")
  card.preferredSize = new Dimension(100, 100)
  card.maximumSize = new Dimension(100, 100)
  card.minimumSize = new Dimension(100, 100)
  card.reactions += {
    case _ => root.update(new Label("Cross clicked"))
  }
  private val centerPanel = new BoxPanel(Orientation.Vertical):
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += card
      contents += Swing.HGlue
    contents += Swing.VGlue
  layout(centerPanel) = BorderPanel.Position.Center
