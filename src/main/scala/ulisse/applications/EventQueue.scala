package ulisse.applications

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the application state. */
trait EventQueue
    extends StationEventQueue
//    with RouteEventQueue
//    with TrainEventQueue
//    with TimetableManager
    with SimulationEventQueue:

  /** Events queue, contains all the events to transform the state. */
  val events: LinkedBlockingQueue[AppState => AppState]

  /** Start the queue. */
  def startProcessing(initState: AppState): Unit

/** Event queue to update the station. */
trait StationEventQueue:
  /** Add an event to create a station. */
  def addStationCreationEvent(event: StationManager => StationManager): Unit

//  /** Add an event to update a station. */
//  def addStationUpdateEvent(event: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit
//
//  /** Add an event to delete a station. */
//  def addStationDeletionEvent(event: (StationManager, RouteManager, TimetableManager) => (StationManager, RouteManager)): Unit

///** Event queue to update the route. */
//trait RouteEventQueue:
//  /** Update route. */
//  def offerUpdateRoute(update: RouteManager => RouteManager): Unit

/** Event queue to update the train. */
//trait TrainEventQueue:
//  /** Update train. */
//  def offerUpdateTrain(update: TrainManager => TrainManager): Unit
//
//  /** Update train. */
//  def offerUpdateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit
//
///** Event queue to update the timetable. */
//trait TimeTableEventQueue:
//  /** Update timetable. */
//  def offerUpdateTimeTable(update: TimetableManager => TimetableManager): Unit

/** Event queue to update the simulation. */
trait SimulationEventQueue:
  /** Update simulation. */
  def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit

object EventQueue:
  /** Create a new queue state. */
  def apply(): EventQueue = new EventQueueImpl()

  private case class EventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends EventQueue:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def startProcessing(initState: AppState): Unit =
      LazyList.continually(events.take()).foldLeft(initState)((state, fun) => fun(state))

//    override def offerUpdateMap(update: (
//        StationManager,
//        RouteManager,
//        TimetableManager
//    ) => (StationManager, RouteManager, TimetableManager)): Unit = events.offer(_.updateMap(update))

    override def addStationCreationEvent(event: StationManager => StationManager): Unit =
      events.offer(_.updateStation(event))

//    override def offerUpdateRoute(update: RouteManager => RouteManager): Unit = ()
//
//
//    override def offerUpdateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit =
//      events.offer(_ updateTrain update)

    override def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_.updateSimulation(update))
