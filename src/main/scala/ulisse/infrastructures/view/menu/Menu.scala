package ulisse.infrastructures.view.menu

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
  Swing
}

def drawCross(g: Graphics2D, center: (Int, Int), length: Int, thickness: Int): Unit =
  val oldColor = g.getColor
  g.setColor(Color.ORANGE)
  g.fillRect(center._1 - thickness / 2, center._2 - length / 2, thickness, length)
  g.fillRect(center._1 - length / 2, center._2 - thickness / 2, length, thickness)
  g.setColor(oldColor)

final case class Card(icon: Graphics2D => Unit, text: String) extends BoxPanel(Orientation.Vertical):
  val button = new Panel {
    override def paint(g: Graphics2D): Unit = icon(g)
  }
  size
  val description = new Label(text)
  val descriptionContainer = new BoxPanel(Orientation.Horizontal):
    contents += Swing.HGlue
    contents += description
    contents += Swing.HGlue
  border = new LineBorder(Color.BLACK, 2)
  contents += button
  contents += descriptionContainer

final case class AppMenu() extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  private val cardCross = (g: Graphics2D) => drawCross(g, (50, 50), 20, 2) // Adjust center point
  val card              = Card(cardCross, "Cross")
  card.preferredSize = new Dimension(100, 100)
  card.maximumSize = new Dimension(100, 100)
  card.minimumSize = new Dimension(100, 100)
  private val centerPanel = new BoxPanel(Orientation.Vertical):
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += card
      contents += Swing.HGlue
    contents += Swing.VGlue
  layout(centerPanel) = BorderPanel.Position.Center
