package ulisse.applications.useCases

import ulisse.applications.managers.SimulationManager
import ulisse.applications.{AppState, SimulationState}
import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.{Executors, LinkedBlockingQueue}
import scala.concurrent.{ExecutionContext, Future, Promise}

final case class SimulationService[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]]
) extends SimulationPorts.Input:
  private val simulationEvents = LinkedBlockingQueue[SimulationState => SimulationState]()
  def start(): Future[Unit] =
    val p = Promise[Unit]()
    simulationEvents.add((state: SimulationState) => {
      p.success(println("[SimulationService]: Simulation Started"));
      state.copy(simulationManager = state.simulationManager.start())
    })
    p.future

  def stop(): Future[Unit] =
    val p = Promise[Unit]()
    simulationEvents.add((state: SimulationState) => {
      p.success(println("[SimulationService]: Simulation Stopped"));
      state.copy(simulationManager = state.simulationManager.stop())
    })
    p.future

  def reset(): Future[Unit] =
    val p = Promise[Unit]()
    simulationEvents.add((state: SimulationState) => {
      p.success(println("[SimulationService]: Simulation Reset"));
      state.copy(simulationManager = state.simulationManager.reset())
    })
    p.future

  Executors.newSingleThreadExecutor().execute(() =>
    LazyList.continually(simulationEvents.take()).foldLeft(SimulationState(SimulationManager()))((state, event) =>
      event(state)
    )
  )
