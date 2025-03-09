package ulisse.applications

import ulisse.applications.event.*
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.entities.train.Trains.TrainTechnology

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the application state, it contains all the events to process. */
trait EventQueue
    extends StationEventQueue
    with RouteEventQueue
    with TrainEventQueue
    with TimeTableEventQueue
    with SimulationEventQueue:

  /** Events queue, contains all the events to process. */
  val events: LinkedBlockingQueue[AppState => AppState]

  private val stationEventQueue    = StationEventQueue(events)
  private val routeEventQueue      = RouteEventQueue(events)
  private val trainEventQueue      = TrainEventQueue(events)
  private val timetableEventQueue  = TimeTableEventQueue(events)
  private val simulationEventQueue = SimulationEventQueue(events)

  export stationEventQueue._, routeEventQueue._, trainEventQueue._, timetableEventQueue._, simulationEventQueue._

  /** Start processing the events. */
  def startProcessing(initState: AppState): Unit

object EventQueue:
  /** Create a new queue state. */
  def apply(): EventQueue = new EventQueueImpl()

  private case class EventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends EventQueue:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def startProcessing(initState: AppState): Unit =
      LazyList.continually(events.take).foldLeft(initState)((state, fun) => fun(state))
