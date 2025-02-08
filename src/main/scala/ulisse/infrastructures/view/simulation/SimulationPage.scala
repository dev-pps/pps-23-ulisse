package ulisse.infrastructures.view.simulation

import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.LayeredContainers.JLayeredPane

import scala.swing.{BorderPanel, BoxPanel, Component, Label, Orientation, Swing}
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.JStyler

import java.awt.{BorderLayout, Color}

trait SimulationPage extends Component
object SimulationPage:
  def apply(): SimulationPage = SimulationPageImpl()

  private final case class SimulationPageImpl() extends SimulationPage with JLayeredPane:
    private val map: Component = Label("Simulation Map").center()
    private val mapControlPane = SimulationPageControlPanel().center()

    mainPane.peer.add(map.peer)
    glassPane.peer.add(mapControlPane.peer, BorderLayout.EAST)

  final case class SimulationPageControlPanel() extends BoxPanel(Orientation.Horizontal):
    private val pageControlImageButtonStyle = JStyler.rectPaletteStyler(
      JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(5, 5), 25),
      JStyler.palette(JStyler.transparentColor, Theme.light.forwardClick.withAlpha(150), Theme.light.forwardClick)
    )
    private val startImage: ImagePanel =
      ImagePanel.createSVGPanel("icons/play.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
        println("Start simulation")
        startImage.visible = false
        pauseImage.visible = true
      ).styler(pageControlImageButtonStyle)
    private val pauseImage: ImagePanel =
      ImagePanel.createSVGPanel("icons/pause.svg", Color.ORANGE).fixedSize(50, 50).visible(false).genericClickReaction(
        () =>
          println("Pause simulation")
          pauseImage.visible = false
          startImage.visible = true
      ).styler(pageControlImageButtonStyle)
    private val resetImage =
      ImagePanel.createSVGPanel("icons/reset.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
        println("Reset simulation")
        startImage.visible = true
        pauseImage.visible = false
      ).styler(pageControlImageButtonStyle)
    background = Theme.light.element
    contents += Swing.HStrut(10)
    contents += startImage
    contents += pauseImage
    contents += Swing.HStrut(10)
    contents += resetImage
    contents += Swing.HStrut(10)
