package ulisse.applications

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}

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

/** Event queue to update the station. */
trait StationEventQueue:
  /** Add an event to read a station. */
  def addReadStationEvent(event: StationManager => StationManager): Unit

  /** Add an event to create a station. */
  def addCreateStationEvent(event: StationManager => StationManager): Unit = addReadStationEvent(event)

  /** Add an event to update a station. */
  def addUpdateStationEvent(event: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit

  /** Add an event to delete a station. */
  def addDeleteStationEvent(event: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): Unit

/** Event queue to update the route. */
trait RouteEventQueue:
  /** Add an event to read a route. */
  def addReadRouteEvent(event: RouteManager => RouteManager): Unit

  /** Add an event to create a route. */
  def addCreateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit

  /** Add an event to update a route. */
  def addUpdateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit =
    addCreateRouteEvent(update)

  /** Add an event to delete a route. */
  def addDeleteRouteEvent(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager)): Unit

/** Event queue to update the train. */
trait TrainEventQueue:
  /** Add an event to create a train. */
  def addReadTrainEvent(update: TrainManager => TrainManager): Unit

  /** Add an event to create a train. */
  def addCreateTrainEvent(update: TrainManager => TrainManager): Unit = addReadTrainEvent(update)

  /** Add an event to update a train. */
  def addUpdateTrainEvent(update: TrainManager => TrainManager): Unit = addReadTrainEvent(update)

  /** Add an event to delete a train. */
  def addDeleteTrainEvent(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit

/** Event queue to update the timetable. */
trait TimeTableEventQueue:
  /** Add an event to read a timetable. */
  def addReadTimetableEvent(update: TimetableManager => TimetableManager): Unit

  /** Add an event to create a timetable. */
  def addCreateTimetableEvent(update: (
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit

  /** Add an event to update a timetable. */
  def addUpdateTimetableEvent(update: (
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit = addCreateTimetableEvent(update)

  /** Add an event to delete a timetable. */
  def addDeleteTimetableEvent(update: TimetableManager => TimetableManager): Unit = addReadTimetableEvent(update)

/** Event queue to update the simulation. */
trait SimulationEventQueue:
  /** Add an event to read a simulation. */
  def addReadSimulationEvent(update: SimulationManager => SimulationManager): Unit

  /** Add an event to create a simulation. */
  def addCreateSimulationEvent(update: (SimulationManager, StationManager) => SimulationManager): Unit

  /** Add an event to update the simulation. */
  def addUpdateSimulationEvent(update: (SimulationManager, StationManager) => SimulationManager): Unit =
    addCreateSimulationEvent(update)

  /** Add an event to delete the simulation. */
  def addDeleteSimulationEvent(update: SimulationManager => SimulationManager): Unit = addReadSimulationEvent(update)

object EventQueue:
  /** Create a new queue state. */
  def apply(): EventQueue = new EventQueueImpl()

  private case class EventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends EventQueue:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def startProcessing(initState: AppState): Unit =
      LazyList.continually(events.take()).foldLeft(initState)((state, fun) => fun(state))

    override def addReadStationEvent(event: StationManager => StationManager): Unit =
      events.offer(_.updateStation(event))

    override def addUpdateStationEvent(event: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit =
      events.offer(_.updateRailwayNetwork(event))

    override def addDeleteStationEvent(event: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit =
      events.offer(_.updateStationSchedule(event))

    override def addReadRouteEvent(event: RouteManager => RouteManager): Unit =
      events.offer(_.updateRoute(event))

    override def addCreateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit =
      events.offer(_.updateRailwayNetwork(update))

    override def addDeleteRouteEvent(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager))
        : Unit = events.offer(_.updateRouteSchedule(update))

    override def addReadTrainEvent(update: TrainManager => TrainManager): Unit = events.offer(_.updateTrain(update))

    override def addDeleteTrainEvent(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager))
        : Unit = events.offer(_.updateTrainSchedule(update))

    override def addReadTimetableEvent(update: TimetableManager => TimetableManager): Unit =
      events.offer(_.updateTimetable(update))

    override def addReadSimulationEvent(update: SimulationManager => SimulationManager): Unit =
      events.offer(_.updateSimulation(update))

    override def addCreateTimetableEvent(update: (
        StationManager,
        RouteManager,
        TrainManager,
        TimetableManager
    ) => (StationManager, RouteManager, TrainManager, TimetableManager)): Unit =
      events.offer(_.updateRailwaySchedule(update))

    override def addCreateSimulationEvent(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_.initSimulation(update))
