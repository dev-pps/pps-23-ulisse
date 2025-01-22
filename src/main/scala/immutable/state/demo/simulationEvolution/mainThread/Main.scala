package immutable.state.demo.simulationEvolution.mainThread

import immutable.state.demo.reference.Monads.Monad
import immutable.state.demo.reference.Monads.Monad.{map2, seqN}
import immutable.state.demo.reference.Streams.Stream
import immutable.state.demo.reference.Streams.Stream.{Cons, Empty}
import immutable.state.demo.simulationEvolution.mainThread.Application.Adapters.SimulationInputAdapter
import immutable.state.demo.simulationEvolution.mainThread.Application.{AppState, SimulationManager}
import immutable.state.demo.simulationEvolution.mainThread.UI.{AppFrame, TestUI}

import java.util.concurrent.LinkedBlockingQueue

@main def main(): Unit =
  val stateEventQueue        = LinkedBlockingQueue[AppState => AppState]
  val simulationInputAdapter = SimulationInputAdapter(stateEventQueue)

  val testUI = TestUI(simulationInputAdapter)
  val app    = AppFrame()
  app.contents = testUI
  app.open()

  val initialState = AppState(SimulationManager(false, 0))
  LazyList.continually(stateEventQueue.take()).scanLeft(initialState)((state, event) =>
    event(state)
  ).foreach(_ => ())
