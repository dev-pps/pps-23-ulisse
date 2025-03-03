package ulisse.infrastructures.view.simulation

import ulisse.adapters.input.SimulationPageAdapter
import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.common.Themes.*
import ulisse.infrastructures.view.components.ExtendedSwing
import ulisse.infrastructures.view.components.ExtendedSwing.SVGPanel
import ulisse.infrastructures.view.utils.ComponentUtils.*

import scala.concurrent.ExecutionContext
import scala.swing.*
import scala.swing.BorderPanel.Position

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

trait SimulationPage extends Component:
  def updateData(data: SimulationData): Unit
  def endSimulation(data: SimulationData): Unit

object SimulationPage:
  def apply(controller: SimulationPageAdapter): SimulationPage = SimulationPageImpl(controller)

  private final case class SimulationPageImpl(controller: SimulationPageAdapter) extends SimulationPage:
    private val mainPane       = new ExtendedSwing.SLayeredPanel()
    private val map: Component = Label("Simulation Map")
    private val menuPanel      = ExtendedSwing.SBorderPanel()
    private val mapControlPane = SimulationPageControlPanel(controller)

    def updateData(data: SimulationData): Unit = mapControlPane.notificationLabel.text =
      s"Step: ${data.step}, Time: ${data.secondElapsed / 1000.0}, Agent: ${data.simulationEnvironment.agents.foldLeft(
          ""
        )((acc, agent) => s"$acc $agent")}"

    menuPanel.layout(mapControlPane) = Position.East

    mainPane.add(map.center())
    mainPane.add(menuPanel)
    def endSimulation(data: SimulationData): Unit = mapControlPane.notificationLabel.text = "Simulation Ended"

  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  final case class SimulationPageControlPanel(controller: SimulationPageAdapter)
      extends BoxPanel(Orientation.Vertical):
//    private val pageControlImageButtonStyle = JStyler.rectPaletteStyler(
//      JStyler.rect(JStyler.defaultSizeRect, JStyler.Dimension2D(5, 5), 25),
//      JStyler.palette(JStyler.transparentColor, Theme.light.forwardClick.withAlpha(150), Theme.light.forwardClick)
//    )
    var timing                   = 0L
    val notificationLabel: Label = Label("Step: 0")

    private val startImage: SVGPanel =
      val image = SVGPanel()
      image.svgIcon = "icons/play.svg"
      image
      // TODO: to integrate event click
//      ImagePanel.createSVGPanel("icons/play.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
//        controller.start().onComplete(_ =>
//          if timing == 0 then timing = System.currentTimeMillis()
//          startImage.visible = false
//          pauseImage.visible = true
//          println("[View]Simulation started")
//        )
//      ) // .styler(pageControlImageButtonStyle)
    private val pauseImage: SVGPanel =
      val image = SVGPanel()
      image.svgIcon = "icons/pause.svg"
      image
//      ImagePanel.createSVGPanel("icons/pause.svg", Color.ORANGE).fixedSize(50, 50).visible(false).genericClickReaction(
//        () =>
//          controller.stop().onComplete(_ =>
//            pauseImage.visible = false
//            startImage.visible = true
//            println(s"[View]Simulation stopped${(System.currentTimeMillis() - timing) / 1000.0}")
//          )
//      ) // .styler(pageControlImageButtonStyle)
    private val resetImage =
      val image = SVGPanel()
      image.svgIcon = "icons/reset.svg"
      image
//      ImagePanel.createSVGPanel("icons/reset.svg", Color.ORANGE).fixedSize(50, 50).genericClickReaction(() =>
//        controller.reset().onComplete(_ =>
//          timing = 0
//          startImage.visible = true
//          pauseImage.visible = false
//          notificationLabel.text = "Step: 0"
//          println(s"[View]Simulation reset${(System.currentTimeMillis() - timing) / 1000}")
//        )
//      ) // .styler(pageControlImageButtonStyle)
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
