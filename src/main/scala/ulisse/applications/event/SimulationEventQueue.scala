package ulisse.applications.event

import ulisse.applications.AppState
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.simulation.data.SimulationData

import java.util.concurrent.LinkedBlockingQueue

final case class SimulationEventData(
    simulationManager: SimulationManager,
    stationManager: StationManager,
    routeManager: RouteManager,
    trainManager: TrainManager,
    timetableManager: TimetableManager
)

/** Event queue to update the simulation. */
trait SimulationEventQueue:
  /** Add an event to read a simulation. */
  def addReadSimulationEvent(update: SimulationData => Unit): Unit

  /** Add an event to create a simulation. */
  def addCreateSimulationEvent(update: SimulationEventData => SimulationManager): Unit

  /** Add an event to update the simulation. */
  def addUpdateSimulationEvent(update: SimulationEventData => SimulationManager): Unit =
    addCreateSimulationEvent(update)

  /** Add an event to delete the simulation. */
  def addDeleteSimulationEvent(update: SimulationManager => SimulationManager): Unit

/** Companion object for the [[SimulationEventQueue]] trait. */
object SimulationEventQueue:
  /** Create a new queue state. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): SimulationEventQueue =
    new SimulationEventQueueImpl(events)

  private case class SimulationEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState])
      extends SimulationEventQueue:
    override def addReadSimulationEvent(update: SimulationData => Unit): Unit = events.offer(_ readSimulation update)

    override def addCreateSimulationEvent(update: SimulationEventData => SimulationManager): Unit =
      events.offer(_ initSimulation update)

    override def addDeleteSimulationEvent(update: SimulationManager => SimulationManager): Unit =
      events.offer(_ updateSimulation update)
