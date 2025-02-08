package ulisse.infrastructures.view.simulation

import ulisse.adapters.input.SimulationPageAdapter
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.LayeredContainers.JLayeredPane

import scala.swing.{BorderPanel, BoxPanel, Component, Label, Orientation, Swing}
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.JStyler

import java.awt.{BorderLayout, Color}

trait SimulationPage extends Component:
  def updateData(): Unit = println("received simulation data update")

object SimulationPage:
  def apply(controller: SimulationPageAdapter): SimulationPage = SimulationPageImpl(controller)

  private final case class SimulationPageImpl(controller: SimulationPageAdapter) extends SimulationPage
      with JLayeredPane:
    private val map: Component = Label("Simulation Map").center()
    private val mapControlPane = SimulationPageControlPanel(controller).center()

    mainPane.peer.add(map.peer)
    glassPane.peer.add(mapControlPane.peer, BorderLayout.EAST)

  final case class SimulationPageControlPanel(controller: SimulationPageAdapter)
      extends BoxPanel(Orientation.Horizontal):
    private val pageControlImageButtonStyle = JStyler.rectPaletteStyler(
      JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(5, 5), 25),
      JStyler.palette(JStyler.transparentColor, Theme.light.forwardClick.withAlpha(150), Theme.light.forwardClick)
    )
    private val startImage: ImagePanel =
      ImagePanel.createSVGPanel("icons/play.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
        startImage.visible = false
        pauseImage.visible = true
        controller.start()
      ).styler(pageControlImageButtonStyle)
    private val pauseImage: ImagePanel =
      ImagePanel.createSVGPanel("icons/pause.svg", Color.ORANGE).fixedSize(50, 50).visible(false).genericClickReaction(
        () =>
          pauseImage.visible = false
          startImage.visible = true
          controller.stop()
      ).styler(pageControlImageButtonStyle)
    private val resetImage =
      ImagePanel.createSVGPanel("icons/reset.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
        startImage.visible = true
        pauseImage.visible = false
        controller.reset()
      ).styler(pageControlImageButtonStyle)
    background = Theme.light.element
    contents += Swing.HStrut(10)
    contents += startImage
    contents += pauseImage
    contents += Swing.HStrut(10)
    contents += resetImage
    contents += Swing.HStrut(10)
