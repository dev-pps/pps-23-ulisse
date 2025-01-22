package immutable.state.demo.asyncAPI.completeFlowDemo

import immutable.state.demo.asyncAPI.completeFlowDemo.Application.Adapters.StationInputAdapter

import scala.concurrent.ExecutionContext
import scala.swing.*

given ExecutionContext = ExecutionContext.fromExecutor: (runnable: Runnable) =>
  Swing.onEDT(runnable.run())

object UI:
  case class TestUI(stationInputAdapter: StationInputAdapter) extends BorderPanel:
    layout(Button("Add Station") {
      this.enabled = false
      stationInputAdapter.stationPortMethodAdd("station").onComplete(r =>
        println(s"Station added $r")
        this.enabled = true
      )
    }) = BorderPanel.Position.West
    layout(Button("Get Station") {
      this.enabled = false
      stationInputAdapter.stationPortMethodGet("station").onComplete(r =>
        println(s"Got Station $r")
        this.enabled = true
      )
    }) = BorderPanel.Position.East
    layout(Button("GetAll Stations") {
      this.enabled = false
      stationInputAdapter.stationPortMethodGetAll().onComplete(r =>
        println(s"GotAll Stations $r")
        this.enabled = true
      )
    }) = BorderPanel.Position.South

  final case class AppFrame() extends MainFrame:
    title = "Station Editor"
    minimumSize = new Dimension(400, 300)
    preferredSize = new Dimension(800, 600)
    pack()
    centerOnScreen()
