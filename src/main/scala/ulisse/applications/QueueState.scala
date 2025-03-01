package ulisse.applications

import ulisse.applications.AppState.{AppStateImpl, Managers}
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.AppState.Managers
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager

import java.util.concurrent.LinkedBlockingQueue

/** Queue state that contains all events to update the application state. */
trait QueueState:
  /** Queue of events to update the application state. */
  val events: LinkedBlockingQueue[AppState => AppState]

  /** Update railway map. */
  def offerUpdateMap(update: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): Unit

  /** Update train. */
  def offerUpdateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit

  /** Start the queue. */
  def play(initState: AppState): Unit

  /** Update a single manager. */
  def updateSingleManager[A <: Managers](update: A => A): Unit

  def offerUpdateStation(update: StationManager => StationManager): Unit

  def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit

object QueueState:
  /** Create a new queue state. */
  def apply(): QueueState = new QueueStateImpl()

  private case class QueueStateImpl(events: LinkedBlockingQueue[AppState => AppState]) extends QueueState:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def offerUpdateMap(update: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit = events.offer(_.updateMap(update))
    override def updateSingleManager[A <: Managers](state: A => A): Unit = ()

    override def offerUpdateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): Unit =
      events.offer(_ updateTrain update)

    override def play(initState: AppState): Unit =
      LazyList.continually(events.take()).foldLeft(initState)((state, fun) => fun(state))

    override def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_.updateSimulation(update))

    override def offerUpdateStation(update: StationManager => StationManager): Unit =
      events.offer(_.updateStation(update))
