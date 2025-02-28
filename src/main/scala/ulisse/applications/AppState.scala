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

object AppState:

  /** Create new application state with empty managers. */
  def apply(): AppState = new AppStateImpl()

  type Managers = StationManager | RouteManager | TrainManager | TimetableManager

  case class AppStateImpl(
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

    def testB[A >: Managers](update: Managers => Managers): AppStateImpl = update match
      case upA: (StationManager => StationManager)     => copy(stationManager = upA(stationManager))
      case upA: (RouteManager => RouteManager)         => copy(routeManager = upA(routeManager))
      case upA: (TrainManager => TrainManager)         => copy(trainManager = upA(trainManager))
      case upA: (TimetableManager => TimetableManager) => copy(timetableManager = upA(timetableManager))

    override def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState =
      copy(simulationManager = update(simulationManager, stationManager))

    override def updateStation(update: StationManager => StationManager): AppState =
      copy(stationManager = update(stationManager))

  final case class SimulationState(simulationManager: SimulationManager)
