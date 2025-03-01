package ulisse.applications

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.AppState.Managers
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager, TimetableManagers}
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

import scala.compiletime.{erasedValue, summonInline}

/** Application state that contains all managers. */
trait AppState:
  val stationManager: StationManager
  val routeManager: RouteManager
  val trainManager: TrainManager
  val timetableManager: TimetableManager
  val simulationManager: SimulationManager

  /** Update simulation manager. */
  def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState

  /** Update station manager. */
  def updateStation(update: StationManager => StationManager): AppState

  /** Update a single manager. */
  def updateManager[M <: Managers](update: M): AppState

  /** Update multiple managers. */
  def updateManagers[M <: Managers](updates: M*): AppState = updates.foldLeft(this)(_.updateManager(_))

object AppState:
  /** Create new application state with empty managers. */
  def apply(): AppState = new AppStateImpl()

  /** [[Managers]] that can be updated. */
  type Managers = StationManager | RouteManager | TrainManager | TimetableManager

  private case class AppStateImpl(
      stationManager: StationManager,
      routeManager: RouteManager,
      trainManager: TrainManager,
      timetableManager: TimetableManager,
      simulationManager: SimulationManager
  ) extends AppState:
    def this() = this(
      StationManager(),
      RouteManager.empty(),
      TrainManager.empty(),
      TimetableManagers.emptyManager(),
      SimulationManager.emptyBatchManager(TimeProviderAdapter(TimeProvider.systemTimeProvider()))
    )

    override def updateManager[M <: Managers](update: M): AppState = update match
      case m: StationManager   => copy(stationManager = m)
      case m: RouteManager     => copy(routeManager = m)
      case m: TrainManager     => copy(trainManager = m)
      case m: TimetableManager => copy(timetableManager = m)
      case _                   => this

    override def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState =
      copy(simulationManager = update(simulationManager, stationManager))

    override def updateStation(update: StationManager => StationManager): AppState =
      copy(stationManager = update(stationManager))

  final case class SimulationState(simulationManager: SimulationManager)
