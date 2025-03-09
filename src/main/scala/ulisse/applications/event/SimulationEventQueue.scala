package ulisse.applications.event

import ulisse.applications.AppState
import ulisse.applications.event.SimulationEventQueue.SimulationManagers
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the simulation. */
trait SimulationEventQueue:
  /** Add an event to read simulation environment. */
  def addReadSimulationEnvironmentEvent(update: RailwayEnvironment => Unit): Unit

  /** Add an event to set up a simulationManager. */
  def addSetupSimulationManagerEvent(update: SimulationManagers => SimulationManager): Unit

  /** Add an event to update the simulationManager. */
  def addUpdateSimulationManagerEvent(update: SimulationManager => SimulationManager): Unit

  /** Add an event to set up the app state. */
  def addSetupAppStateEvent(update: AppState => AppState): Unit

/** Companion object for the [[SimulationEventQueue]] trait. */
object SimulationEventQueue:
  /** Simulation managers. */
  final case class SimulationManagers(
      simulationManager: SimulationManager,
      stationManager: StationManager,
      routeManager: RouteManager,
      trainManager: TrainManager,
      timetableManager: TimetableManager
  )

  /** Create a new queue state. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): SimulationEventQueue = SimulationEventQueueImpl(events)

  private case class SimulationEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState])
      extends SimulationEventQueue:
    override def addReadSimulationEnvironmentEvent(update: RailwayEnvironment => Unit): Unit =
      events.offer(_ readSimulationData (sd => update(sd.simulationEnvironment)))
    override def addSetupSimulationManagerEvent(update: SimulationManagers => SimulationManager): Unit =
      events.offer(_ setupSimulationManager update)
    override def addUpdateSimulationManagerEvent(update: SimulationManager => SimulationManager): Unit =
      events.offer(_ updateSimulationManager update)
    override def addSetupAppStateEvent(update: AppState => AppState): Unit = events.offer(update)
