package ulisse.applications.event

import ulisse.applications.AppState
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.simulation.data.SimulationData
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment

import java.util.concurrent.LinkedBlockingQueue

final case class SimulationManagers(
    simulationManager: SimulationManager,
    stationManager: StationManager,
    routeManager: RouteManager,
    trainManager: TrainManager,
    timetableManager: TimetableManager
)

/** Event queue to update the simulation. */
trait SimulationEventQueue:
  /** Add an event to read simulation environment. */
  def addReadSimulationEnvironmentEvent(update: RailwayEnvironment => Unit): Unit

  /** Add an event to create a simulation. */
  def addSetupSimulationManagerEvent(update: SimulationManagers => SimulationManager): Unit

  /** Add an event to update the simulation. */
  def addUpdateSimulationManagerEvent(update: SimulationManager => SimulationManager): Unit

/** Companion object for the [[SimulationEventQueue]] trait. */
object SimulationEventQueue:
  /** Create a new queue state. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): SimulationEventQueue = SimulationEventQueueImpl(events)

  private case class SimulationEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState])
      extends SimulationEventQueue:

    override def addReadSimulationEnvironmentEvent(update: RailwayEnvironment => Unit): Unit =
      events.offer(_ readSimulationData (sd => update(sd.simulationEnvironment)))

    override def addSetupSimulationManagerEvent(update: SimulationManagers => SimulationManager): Unit =
      events.offer(_ setupSimulation update)

    override def addUpdateSimulationManagerEvent(update: SimulationManager => SimulationManager): Unit =
      events.offer(_ updateSimulation update)
