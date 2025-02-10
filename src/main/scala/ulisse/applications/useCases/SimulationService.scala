package ulisse.applications.useCases

import ulisse.applications.managers.{SimulationEnvironment, SimulationManager}
import ulisse.applications.{AppState, SimulationState}
import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station

import java.util.concurrent.{Executors, LinkedBlockingQueue}
import scala.concurrent.{ExecutionContext, Future, Promise}

final case class SimulationService[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    eventQueue: LinkedBlockingQueue[AppState[N, C, S] => AppState[N, C, S]],
    notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  private val simulationEvents = LinkedBlockingQueue[SimulationState => SimulationState]()
  def start(): Future[Unit] =
    val p = Promise[Unit]()
    eventQueue.add((appState: AppState[N, C, S]) => {
      simulationEvents.add((state: SimulationState) => {
        p.success(println("[SimulationService]: Simulation Started"));
        doStep()
        state.copy(simulationManager =
          state.simulationManager.start(SimulationEnvironment(appState.stationManager.stations))
        )
      })
      appState
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

  @SuppressWarnings(Array("org.wartremover.warts.Recursion"))
  private def doStep(): Unit =
    simulationEvents.offer((state: SimulationState) => {
      if state.simulationManager.running then
        doStep()
        println("[SimulationService]: Simulation Step")
        state.copy(simulationManager = state.simulationManager.doStep())
      else
        state
    })

  Executors.newSingleThreadExecutor().execute(() =>
    LazyList.continually(simulationEvents.take()).foldLeft(SimulationState(SimulationManager(notificationService)))(
      (state, event) =>
        event(state)
    )
  )
