package ulisse.applications

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
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

  /** Simulation manager. */
  val simulationManager: SimulationManager

  /** Update [[StationManager]] with a function. */
  def updateStation(update: StationManager => StationManager): AppState

  /** Update [[TrainManager]] with a function. */
  def updateTrain(update: TrainManager => TrainManager): AppState

  /** Update [[TimetableManager]] with a function. */
  def updateTimetable(update: TimetableManager => TimetableManager): AppState

  /** Update [[StationManager]] and [[RouteManager]] with a function. */
  def updateRailwayNetwork(update: (StationManager, RouteManager) => (StationManager, RouteManager)): AppState

  /** Update [[StationManager]] and [[RouteManager]] and [[TimetableManager]] with a function. */
  def updateStationSchedule(update: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): AppState

  /** Update [[RouteManager]] and [[TimetableManager]] with a function. */
  def updateRouteSchedule(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager)): AppState

  /** Update [[TrainManager]] and [[TimetableManager]] with a function. */
  def updateTrainSchedule(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): AppState

  /** Update [[StationManager]], [[RouteManager]] and [[TrainManager]] with a function. */
  def updateRailway(update: (
      StationManager,
      RouteManager,
      TrainManager
  ) => (StationManager, RouteManager, TrainManager)): AppState

  /** update [[SimulationManager]], with a function that takes a [[SimulationManager]] and a [[StationManager]]. */
  def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState

object AppState:
  /** Create new application state with empty managers. */
  def apply(): AppState = new AppStateImpl()

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

    override def updateStation(update: StationManager => StationManager): AppState =
      copy(stationManager = update(stationManager))

    override def updateTrain(update: TrainManager => TrainManager): AppState =
      copy(trainManager = update(trainManager))

    override def updateTimetable(update: TimetableManager => TimetableManager): AppState =
      copy(timetableManager = update(timetableManager))

    override def updateRailwayNetwork(update: (StationManager, RouteManager) => (StationManager, RouteManager))
        : AppState =
      val (newStation, newRoute) = update(stationManager, routeManager)
      copy(stationManager = newStation, routeManager = newRoute)

    override def updateStationSchedule(update: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): AppState =
      val (newStation, newRoute, newTimetable) = update(stationManager, routeManager, timetableManager)
      copy(stationManager = newStation, routeManager = newRoute, timetableManager = newTimetable)

    override def updateRouteSchedule(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager))
        : AppState =
      val (newRoute, newTimetable) = update(routeManager, timetableManager)
      copy(routeManager = newRoute, timetableManager = newTimetable)

    override def updateTrainSchedule(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager))
        : AppState =
      val (newTrainManager, newTimetableManager) = update(trainManager, timetableManager)
      copy(trainManager = newTrainManager, timetableManager = newTimetableManager)

    override def updateRailway(update: (
        StationManager,
        RouteManager,
        TrainManager
    ) => (StationManager, RouteManager, TrainManager)): AppState =
      val (newStation, newRoute, newTimetable) = update(stationManager, routeManager, trainManager)
      copy(stationManager = newStation, routeManager = newRoute, trainManager = newTimetable)

    override def updateSimulation(update: (SimulationManager, StationManager) => SimulationManager): AppState =
      copy(simulationManager = update(simulationManager, stationManager))
