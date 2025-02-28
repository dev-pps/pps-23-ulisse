package ulisse.applications

import ulisse.applications.AppState
import ulisse.applications.managers.{SimulationManager, StationManager}

import java.util.concurrent.LinkedBlockingQueue

trait QueueState:

  val events: LinkedBlockingQueue[AppState => AppState]

  def offerUpdateStation(update: StationManager => StationManager): Unit
  def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit

object QueueState:
  def apply(): QueueState = new QueueStateImpl()

  private case class QueueStateImpl(events: LinkedBlockingQueue[AppState => AppState]) extends QueueState:
    def this() = this(LinkedBlockingQueue[AppState => AppState]())

    def testA[A <: AppState.Managers](state: AppState.Managers => AppState.Managers): Unit = ()

    override def offerUpdateSimulation(update: (SimulationManager, StationManager) => SimulationManager): Unit =
      events.offer(_.updateSimulation(update))

    override def offerUpdateStation(update: StationManager => StationManager): Unit =
      events.offer(_.updateStation(update))
