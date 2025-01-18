package immutable.state.demo.immutableApplication

import immutable.state.demo.immutableApplication.Application.Adapters.{RouteInputAdapter, StationInputAdapter}
import immutable.state.demo.immutableApplication.Application.{AppState, RouteManager, StationManager}
import immutable.state.demo.immutableApplication.Monads.Monad
import immutable.state.demo.immutableApplication.Monads.Monad.{map2, seqN}
import immutable.state.demo.immutableApplication.States.State
import immutable.state.demo.immutableApplication.Streams.Stream
import immutable.state.demo.immutableApplication.Streams.Stream.{Cons, Empty}
import immutable.state.demo.immutableApplication.UI.{AppFrame, TestUI}

import java.util.concurrent.LinkedBlockingQueue

//def mv[SM, SV, AM, AV](m1: State[SM, AM], f: AM => State[SV, AV]): State[(SM, SV), AV] =
//  State: (sm, sv) =>
//    val (sm2, am) = m1.run(sm)
//    val (sv2, av) = f(am).run(sv)
//    ((sm2, sv2), av)

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
//  LazyList.continually(stateEventQueue.take()).scanLeft(initialState)((state, event) =>
//    event(state)
//  ).foreach((appState: AppState) =>
//    println(s"Stations: ${appState.stationManager.stations.length}, Routes: ${appState.routeManager.routes.length}")
//  )
