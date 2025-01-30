package ulisse.infrastructures.view.menu

import scaredOfArchunit.StationSettings
import ulisse.infrastructures.view.UpdatableContainer
import ulisse.infrastructures.view.simulation.MenuBar

import java.awt.Color
import javax.swing.border.LineBorder
import scala.swing.event.MouseClicked
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
  listenTo()

final case class AppMenu(root: UpdatableContainer) extends BorderPanel:
  preferredSize = new Dimension(600, 400)
  val card = Card(drawCross(20, 2), "Cross")
  card.preferredSize = new Dimension(100, 100)
  card.maximumSize = new Dimension(100, 100)
  card.minimumSize = new Dimension(100, 100)
  card.listenTo(card.mouse.clicks)
  card.reactions += {
    case MouseClicked(_, _, _, _, _) =>
      root.update(StationSettings().stationEditorView)
  }

  val menuBar = MenuBar()
  menuBar.preferredSize = new Dimension(600, 50)
  menuBar.maximumSize = new Dimension(600, 50)
  menuBar.minimumSize = new Dimension(600, 50)
  layout(menuBar) = BorderPanel.Position.North

  val sideMenu = new BoxPanel(Orientation.Vertical):
    preferredSize = new Dimension(100, 400)
    maximumSize = new Dimension(100, 400)
    minimumSize = new Dimension(100, 400)
    background = Color.DARK_GRAY
    contents += Swing.VStrut(10)
    contents += new Label("Side Menu")
    contents += Swing.VStrut(10)

  layout(sideMenu) = BorderPanel.Position.West

  private val centerPanel = new BoxPanel(Orientation.Vertical):
    contents += Swing.VGlue
    contents += new BoxPanel(Orientation.Horizontal):
      contents += Swing.HGlue
      contents += card
      contents += Swing.HGlue
    contents += Swing.VGlue
  layout(centerPanel) = BorderPanel.Position.Center
