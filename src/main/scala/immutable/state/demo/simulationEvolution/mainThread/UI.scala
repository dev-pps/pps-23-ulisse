package immutable.state.demo.simulationEvolution.mainThread

import immutable.state.demo.simulationEvolution.mainThread.Application.Adapters.SimulationInputAdapter

import scala.concurrent.ExecutionContext
import scala.swing.*

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

object UI:
  case class TestUI(simulationInputAdapter: SimulationInputAdapter) extends BorderPanel:
    layout(Button("Start Simulation") {
      this.enabled = false
      simulationInputAdapter.start().onComplete(_ =>
        println("Simulation started")
        this.enabled = true
      )
    }) = BorderPanel.Position.West
    layout(Button("Stop Simulation") {
      this.enabled = false
      simulationInputAdapter.stop().onComplete(_ =>
        println("Simulation stopped")
        this.enabled = true
      )
    }) = BorderPanel.Position.East

  final case class AppFrame() extends MainFrame:
    title = "Station Editor"
    minimumSize = new Dimension(400, 300)
    preferredSize = new Dimension(800, 600)
    pack()
    centerOnScreen()
