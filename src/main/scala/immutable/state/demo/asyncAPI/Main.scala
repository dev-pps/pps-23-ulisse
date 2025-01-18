package immutable.state.demo.asyncAPI

import immutable.state.demo.asyncAPI.Application.Adapters.{RouteInputAdapter, StationInputAdapter}
import immutable.state.demo.asyncAPI.Application.{AppState, RouteManager, StationManager}
import immutable.state.demo.asyncAPI.UI.{AppFrame, TestUI}

import java.util.concurrent.LinkedBlockingQueue

@main def main(): Unit =
  val stateEventQueue     = LinkedBlockingQueue[AppState => AppState]
  val stationInputAdapter = StationInputAdapter(stateEventQueue)
  val routeInputAdapter   = RouteInputAdapter(stateEventQueue)

  val testUI = TestUI(stationInputAdapter, routeInputAdapter)
  val app    = AppFrame()
  app.contents = testUI
  app.open()

  val initialState = AppState(StationManager(List.empty), RouteManager(List.empty))

  LazyList.continually(stateEventQueue.take()).foreach(event =>
    println("Event")
    event(initialState)
  )
