package ulisse.applications

import ulisse.applications.EventQueues._
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.simulation.Simulations.SimulationData
import ulisse.entities.train.Trains.TrainTechnology

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the application state. */
trait EventQueue
    extends StationEventQueue
    with RouteEventQueue
    with TrainEventQueue
    with TimeTableEventQueue
    with SimulationEventQueue:

  /** Events queue, contains all the events to process. */
  val events: LinkedBlockingQueue[AppState => AppState]

  /** Start processing the events. */
  def startProcessing(initState: AppState): Unit

object EventQueue:
  /** Create a new queue state. */
  def apply(): EventQueue = new EventQueueImpl()

  private case class EventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends EventQueue:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def startProcessing(initState: AppState): Unit =
      LazyList.continually(events.take).foldLeft(initState)((state, fun) => fun(state))

    override def addReadStationEvent(event: StationManager => Unit): Unit = events.offer(_ readStation event)

    override def addCreateStationEvent(event: StationManager => StationManager): Unit =
      events.offer(_ updateStation event)

    override def addUpdateStationEvent(event: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit = events.offer(_ updateStationSchedule event)

    override def addReadRouteEvent(event: RouteManager => Unit): Unit = events.offer(_ readRoute event)

    override def addCreateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit =
      events.offer(_ updateRailwayNetwork update)

    override def addDeleteRouteEvent(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager))
        : Unit = events.offer(_ updateRouteSchedule update)

    override def addReadTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => Unit): Unit =
      events.offer(_ readTrain update)

    def addCreateTrainEvent(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): Unit =
      events.offer(_ updateTrain update)

    override def addDeleteTrainEvent(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager))
        : Unit = events.offer(_ updateTrainSchedule update)

    override def addReadTimetableEvent(update: TimetableManager => Unit): Unit = events.offer(_ readTimetable update)

    override def addCreateTimetableEvent(update: (
        StationManager,
        RouteManager,
        TrainManager,
        TimetableManager
    ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit =
      events.offer(_ updateRailwaySchedule update)

    override def addDeleteTimetableEvent(update: TimetableManager => TimetableManager): Unit =
      events.offer(_ updateTimetable update)

    override def addReadSimulationEvent(update: SimulationData => Unit): Unit = events.offer(_ readSimulation update)

    override def addCreateSimulationEvent(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_ initSimulation update)

    override def addDeleteSimulationEvent(update: SimulationManager => SimulationManager): Unit =
      events.offer(_ updateSimulation update)
