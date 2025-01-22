package immutable.state.demo.asyncAPI.simpleDemo

import immutable.state.demo.asyncAPI.simpleDemo.Application.Adapters.{RouteInputAdapter, StationInputAdapter}
import immutable.state.demo.asyncAPI.simpleDemo.Application.{AppState, RouteManager, StationManager}
import immutable.state.demo.asyncAPI.simpleDemo.UI.{AppFrame, TestUI}
import immutable.state.demo.reference.Monads.Monad
import immutable.state.demo.reference.Monads.Monad.{map2, seqN}
import immutable.state.demo.reference.Streams.Stream
import immutable.state.demo.reference.Streams.Stream.{Cons, Empty}

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
  LazyList.continually(stateEventQueue.take()).scanLeft(initialState)((state, event) =>
    event(state)
  ).foreach((appState: AppState) =>
    println(s"Stations: ${appState.stationManager.stations.length}, Routes: ${appState.routeManager.routes.length}")
  )
