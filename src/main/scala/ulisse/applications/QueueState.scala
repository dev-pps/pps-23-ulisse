package ulisse.applications

import ulisse.applications.AppState.{AppStateImpl, Managers}
import ulisse.applications.managers.{SimulationManager, StationManager}

import java.util.concurrent.LinkedBlockingQueue

/** Queue state that contains all events to update the application state. */
trait QueueState:
  val events: LinkedBlockingQueue[AppState => AppState]

  /** Start the queue. */
  def play(initState: AppState): Unit

  /** Update a single manager. */
  def updateSingleManager[A <: Managers](update: A => A): Unit

  def offerUpdateStation(update: StationManager => StationManager): Unit

  def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit

object QueueState:
  def apply(): QueueState = new QueueStateImpl()

  private case class QueueStateImpl(events: LinkedBlockingQueue[AppState => AppState]) extends QueueState:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    override def updateSingleManager[A <: Managers](state: A => A): Unit =
      events.offer(_.updateSingleManager(state))

    override def play(initState: AppState): Unit =
      LazyList.continually(events.take()).foldLeft(initState)((state, fun) => fun(state))
    override def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_.updateSimulation(update))

    override def offerUpdateStation(update: StationManager => StationManager): Unit =
      events.offer(_.updateStation(update))
