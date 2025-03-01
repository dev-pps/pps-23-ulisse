package ulisse.applications

import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager}

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the application state. */
trait EventQueue:

  /** Events queue, contains all the events to transform the state. */
  val events: LinkedBlockingQueue[AppState => AppState]

  /** Start the queue. */
  def play(initState: AppState): Unit

  /** Update railway map. */
  def offerUpdateMap(update: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): Unit

  /** Update train. */
  def offerUpdateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit

  /** Update station. */
  def offerUpdateStation(update: StationManager => StationManager): Unit

  /** Update simulation. */
  def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit

object EventQueue:
  /** Create a new queue state. */
  def apply(): EventQueue = new EventQueueImpl()

  private case class EventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends EventQueue:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def play(initState: AppState): Unit =
      LazyList.continually(events.take()).foldLeft(initState)((state, fun) => fun(state))

    override def offerUpdateMap(update: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit = events.offer(_.updateMap(update))

    override def offerUpdateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit =
      events.offer(_ updateTrain update)

    override def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_.updateSimulation(update))

    override def offerUpdateStation(update: StationManager => StationManager): Unit =
      events.offer(_.updateStation(update))
