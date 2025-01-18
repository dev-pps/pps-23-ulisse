package immutable.state.demo.asyncAPI

import immutable.state.demo.asyncAPI.Application.Adapters.{RouteInputAdapter, StationInputAdapter}

import scala.concurrent.ExecutionContext
import scala.swing.{BorderPanel, Button, Dimension, MainFrame, Swing}

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

object UI:
  case class TestUI(stationInputAdapter: StationInputAdapter, routeInputAdapter: RouteInputAdapter) extends BorderPanel:
    layout(Button("Add Station") {
      this.enabled = false
      stationInputAdapter.stationPortMethod("station").onComplete(_ =>
        println("Station added")
        this.enabled = true
      )
    }) = BorderPanel.Position.West
    layout(Button("Add Route") {
      this.enabled = false
      routeInputAdapter.routePortMethod(1).onComplete(_ =>
        println("Route added")
        this.enabled = true
      )
    }) = BorderPanel.Position.East

  final case class AppFrame() extends MainFrame:
    title = "Station Editor"
    minimumSize = new Dimension(400, 300)
    preferredSize = new Dimension(800, 600)
    pack()
    centerOnScreen()
