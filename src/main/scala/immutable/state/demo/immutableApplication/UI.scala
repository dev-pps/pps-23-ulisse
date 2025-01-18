package immutable.state.demo.immutableApplication

import immutable.state.demo.immutableApplication.Application.Adapters.{RouteInputAdapter, StationInputAdapter}
import immutable.state.demo.immutableApplication.Application.AppState
import immutable.state.demo.reference.States.State

import java.util.concurrent.LinkedBlockingQueue
import scala.swing.*

object UI:
  case class TestUI(
      stationInputAdapter: StationInputAdapter,
      routeInputAdapter: RouteInputAdapter,
      stateEventQueue: LinkedBlockingQueue[() => State[AppState, ?]]
  ) extends BorderPanel:
    layout(StationUI(stationInputAdapter, stateEventQueue)) = BorderPanel.Position.West
    layout(RouteUI(routeInputAdapter, stateEventQueue)) = BorderPanel.Position.East

  case class StationUI(
      stationInputAdapter: StationInputAdapter,
      stateEventQueue: LinkedBlockingQueue[() => State[AppState, ?]]
  ) extends BorderPanel:
    layout(Button("Add Station") {
      stateEventQueue.offer(() =>
        updateViewGivenAppState(
          stationInputAdapter.stationPortMethod("station"),
          (stateAction => println(s"StationUI: $stateAction"))
        )
      )
    }) = BorderPanel.Position.West

  case class RouteUI(
      routeInputAdapter: RouteInputAdapter,
      stateEventQueue: LinkedBlockingQueue[() => State[AppState, ?]]
  ) extends BorderPanel:
    layout(Button("Add Route") {
      stateEventQueue.offer(() =>
        updateViewGivenAppState(
          routeInputAdapter.routePortMethod(1),
          (stateAction => println(s"RouteUI: $stateAction"))
        )
      )
    }) = BorderPanel.Position.East

  final case class AppFrame() extends MainFrame:
    title = "Station Editor"
    minimumSize = new Dimension(400, 300)
    preferredSize = new Dimension(800, 600)
    pack()
    centerOnScreen()
