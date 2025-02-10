package ulisse.infrastructures.view.simulation

import ulisse.adapters.input.SimulationPageAdapter
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.LayeredContainers.JLayeredPane

import scala.swing.{BorderPanel, BoxPanel, Component, Label, Orientation, Swing}
import ulisse.infrastructures.view.components.ComponentUtils.*
import ulisse.infrastructures.view.components.ImagePanels.ImagePanel
import ulisse.infrastructures.view.components.JStyler

import java.awt.{BorderLayout, Color}
import scala.concurrent.ExecutionContext

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

trait SimulationPage extends Component:
  def updateData(step: Int): Unit

object SimulationPage:
  def apply(controller: SimulationPageAdapter): SimulationPage = SimulationPageImpl(controller)

  private final case class SimulationPageImpl(controller: SimulationPageAdapter) extends SimulationPage
      with JLayeredPane:
    private val map: Component = Label("Simulation Map")
    private val mapControlPane = SimulationPageControlPanel(controller)

    def updateData(step: Int): Unit = mapControlPane.notificationLabel.text = s"Step: $step"

    mainPane.peer.add(map.center().peer)
    glassPane.peer.add(mapControlPane.center().peer, BorderLayout.EAST)

  final case class SimulationPageControlPanel(controller: SimulationPageAdapter)
      extends BoxPanel(Orientation.Vertical):
    private val pageControlImageButtonStyle = JStyler.rectPaletteStyler(
      JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(5, 5), 25),
      JStyler.palette(JStyler.transparentColor, Theme.light.forwardClick.withAlpha(150), Theme.light.forwardClick)
    )
    val notificationLabel: Label = Label("Step: 0")
    private val startImage: ImagePanel =
      ImagePanel.createSVGPanel("icons/play.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
        controller.start().onComplete(_ =>
          startImage.visible = false
          pauseImage.visible = true
          println("[View]Simulation started")
        )
      ).styler(pageControlImageButtonStyle)
    private val pauseImage: ImagePanel =
      ImagePanel.createSVGPanel("icons/pause.svg", Color.ORANGE).fixedSize(50, 50).visible(false).genericClickReaction(
        () =>
          controller.stop().onComplete(_ =>
            pauseImage.visible = false
            startImage.visible = true
            println("[View]Simulation stopped")
          )
      ).styler(pageControlImageButtonStyle)
    private val resetImage =
      ImagePanel.createSVGPanel("icons/reset.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
        controller.reset().onComplete(_ =>
          startImage.visible = true
          pauseImage.visible = false
          notificationLabel.text = "Step: 0"
          println("[View]Simulation reset")
        )
      ).styler(pageControlImageButtonStyle)
    background = Theme.light.element
    contents += Swing.VGlue
    contents += notificationLabel.centerHorizontally()
    contents += Swing.VStrut(10)
    contents += new BoxPanel(Orientation.Horizontal) {
      opaque = false
      contents += Swing.HStrut(10)
      contents += startImage
      contents += pauseImage
      contents += Swing.HStrut(10)
      contents += resetImage
      contents += Swing.HStrut(10)
    }
    contents += Swing.VGlue
