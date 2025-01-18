package immutable.state.demo.immutableApplication

import immutable.state.demo.immutableApplication.Application.Adapters.{RouteInputAdapter, StationInputAdapter}
import immutable.state.demo.immutableApplication.Application.{AppState, RouteManager, StationManager}
import immutable.state.demo.reference.Monads.Monad
import immutable.state.demo.reference.Monads.Monad.{map2, seqN}
import immutable.state.demo.reference.States.State
import immutable.state.demo.reference.Streams.Stream
import immutable.state.demo.reference.Streams.Stream.{Cons, Empty}
import immutable.state.demo.immutableApplication.UI.{AppFrame, TestUI}

import java.util.concurrent.LinkedBlockingQueue

def updateViewGivenAppState[AppState, AppAction](
    incomingAppState: State[AppState, AppAction],
    viewUpdate: AppAction => Unit
): State[AppState, AppAction] =
  State: appState =>
    val (newAppState, newAppAction) = incomingAppState.run(appState)
    viewUpdate(newAppAction)
    (newAppState, newAppAction)

@main def main(): Unit =
  val stateEventQueue     = LinkedBlockingQueue[() => State[AppState, ?]]()
  val stationInputAdapter = StationInputAdapter()
  val routeInputAdapter   = RouteInputAdapter()

  val testUI = TestUI(stationInputAdapter, routeInputAdapter, stateEventQueue)
  val app    = AppFrame()
  app.contents = testUI
  app.open()

  val initialState = AppState(StationManager(List.empty), RouteManager(List.empty))
  LazyList.continually(stateEventQueue.take()).scanLeft(initialState)((state, event) =>
    event().run(state)._1
  ).foreach((appState: AppState) =>
    println(s"Stations: ${appState.stationManager.stations.length}, Routes: ${appState.routeManager.routes.length}")
  )
