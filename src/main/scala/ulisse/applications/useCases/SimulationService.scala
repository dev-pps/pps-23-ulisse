package ulisse.applications.useCases

import ulisse.applications.managers.SimulationManager
import ulisse.applications.{AppState, SimulationState}
import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinate
import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.station.Station

import java.util.concurrent.{Executors, LinkedBlockingQueue}
import scala.concurrent.{ExecutionContext, Future, Promise}

final case class SimulationAgent(timeUpdatePerSecond: Double, spaceTravelled: Double, velocity: Double):
  /* dt is time elapsed between each frame so
       dt * timeUpdatePerSecond is the fraction of
       the movement that has to be done
       if dt > 1 then the simulation could break
   */
  def update(dt: Double): SimulationAgent =
    val maxDelta = math.min(dt, 1.0)
    println(
      s"[SimulationAgent]: Update $dt, $dt, $velocity, ${dt * velocity}, ${timeUpdatePerSecond * dt * velocity} ${spaceTravelled + timeUpdatePerSecond * dt * velocity}"
    )
    copy(spaceTravelled = spaceTravelled + velocity * dt * timeUpdatePerSecond)

final case class SimulationData(step: Int, secondElapsed: Double, simulationAgent: SimulationAgent)

final case class SimulationService(
    eventQueue: LinkedBlockingQueue[AppState => AppState],
    notificationService: SimulationPorts.Output
) extends SimulationPorts.Input:
  private val simulationEvents = LinkedBlockingQueue[SimulationState => SimulationState]()
  def start(): Future[Unit] =
    val p = Promise[Unit]()
    eventQueue.add((appState: AppState) => {
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
