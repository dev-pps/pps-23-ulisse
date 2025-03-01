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
  /** Station manager. */
  val stationManager: StationManager

  /** Route manager. */
  val routeManager: RouteManager

  /** Train manager. */
  val trainManager: TrainManager

  /** Timetable manager. */
  val timetableManager: TimetableManager
  val simulationManager: SimulationManager

  /** Update simulation manager. */
  def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState

  /** Update station manager. */
  def updateStation(update: StationManager => StationManager): AppState

  /** Update railway map. */
  def updateMap(update: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): AppState

  /** Update train. */
  def updateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): AppState

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

    override def updateMap(update: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): AppState =
      val (newStation, newRoute, newTimetable) = update(stationManager, routeManager, timetableManager)
      copy(stationManager = newStation, routeManager = newRoute, timetableManager = newTimetable)

    override def updateTrain(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): AppState =
      val (newTrainManager, newTimetableManager) = update(trainManager, timetableManager)
      copy(trainManager = newTrainManager, timetableManager = newTimetableManager)

    override def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState =
      copy(simulationManager = update(simulationManager, stationManager))

    override def updateStation(update: StationManager => StationManager): AppState =
      copy(stationManager = update(stationManager))

  final case class SimulationState(simulationManager: SimulationManager)
