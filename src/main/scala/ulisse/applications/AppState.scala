package ulisse.applications

import ulisse.adapters.output.UtilityAdapters.TimeProviderAdapter
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.applications.managers.{SimulationManager, StationManager, TimetableManagers}
import ulisse.entities.simulation.data.{EngineConfiguration, SimulationData}
import ulisse.entities.train.Trains.TrainTechnology
import ulisse.infrastructures.commons.TimeProviders.TimeProvider

import java.lang.module.Configuration
import scala.compiletime.{erasedValue, summonInline}

/** Application state that contains all managers. */
trait AppState:
  /** Station manager. */
  val stationManager: StationManager

  /** Route manager. */
  val routeManager: RouteManager

  /** Technology manager. */
  val technologyManager: TechnologyManager[TrainTechnology]

  /** Train manager. */
  val trainManager: TrainManager

  /** Timetable manager. */
  val timetableManager: TimetableManager

  /** Simulation manager. */
  val simulationManager: SimulationManager

  /** Read [[StationManager]]. */
  def readStation(read: StationManager => Unit): AppState = { read(stationManager); this }

  /** Read [[RouteManager]]. */
  def readRoute(read: RouteManager => Unit): AppState = { read(routeManager); this }

  /** Read [[TrainManager]]. */
  def readTrain(read: (TrainManager, TechnologyManager[TrainTechnology]) => Unit): AppState =
    read(trainManager, technologyManager)
    this

  /** Read [[TimetableManager]]. */
  def readTimetable(read: TimetableManager => Unit): AppState = { read(timetableManager); this }

  /** Read [[SimulationManager]]. */
  def readSimulation(read: SimulationData => Unit): AppState = { read(simulationManager.simulationData); this }

  /** Update [[StationManager]]. */
  def updateStation(update: StationManager => StationManager): AppState

  /** Update [[RouteManager]]. */
  def updateRoute(update: RouteManager => RouteManager): AppState

  /** Update TechnologyManager. */
  def updateTechnology(update: TechnologyManager[TrainTechnology] => TechnologyManager[TrainTechnology]): AppState

  /** Update [[TrainManager]]. */
  def updateTrain(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): AppState

  /** Update [[TimetableManager]]. */
  def updateTimetable(update: TimetableManager => TimetableManager): AppState

  /** Update [[SimulationManager]]. */
  def updateSimulation(update: SimulationManager => SimulationManager): AppState

  /** Update [[StationManager]], [[RouteManager]]. */
  def updateRailwayNetwork(update: (StationManager, RouteManager) => (StationManager, RouteManager)): AppState

  /** Update [[StationManager]], [[RouteManager]] and [[TimetableManager]]. */
  def updateStationSchedule(update: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): AppState

  /** Update [[RouteManager]] and [[TimetableManager]]. */
  def updateRouteSchedule(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager)): AppState

  /** Update [[TrainManager]] and [[TimetableManager]]. */
  def updateTrainSchedule(update: (TrainManager, TimetableManager) => (TrainManager, TimetableManager)): AppState

  /** Update [[StationManager]], [[RouteManager]] and [[TrainManager]]. */
  def updateRailwaySchedule(update: (
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) => (StationManager, RouteManager, TrainManager, TimetableManager)): AppState

  /** update [[SimulationManager]], with a function that takes a [[SimulationManager]] and a [[StationManager]]. */
  def initSimulation(update: (
      SimulationManager,
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) => SimulationManager): AppState

object AppState:
  /** Create new application state with empty managers. */
  def apply(): AppState = new AppStateImpl().updateTechnology(_ => TechnologyManager.createTrainTechnology())

  private case class AppStateImpl(
      stationManager: StationManager,
      routeManager: RouteManager,
      trainManager: TrainManager,
      technologyManager: TechnologyManager[TrainTechnology],
      timetableManager: TimetableManager,
      simulationManager: SimulationManager
  ) extends AppState:
    def this() = this(
      StationManager(),
      RouteManager.empty(),
      TrainManager.empty(),
      TechnologyManager.empy(),
      TimetableManagers.empty(),
      SimulationManager.defaultBatchManager(TimeProviderAdapter(TimeProvider.systemTimeProvider()))
    )

    override def updateStation(update: StationManager => StationManager): AppState =
      copy(stationManager = update(stationManager))

    override def updateRoute(update: RouteManager => RouteManager): AppState =
      copy(routeManager = update(routeManager))

    override def updateTechnology(update: TechnologyManager[TrainTechnology] => TechnologyManager[TrainTechnology])
        : AppState = copy(technologyManager = update(technologyManager))

    override def updateTrain(update: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager): AppState =
      copy(trainManager = update(trainManager, technologyManager))

    override def updateTimetable(update: TimetableManager => TimetableManager): AppState =
      copy(timetableManager = update(timetableManager))

    override def updateSimulation(update: SimulationManager => SimulationManager): AppState =
      copy(simulationManager = update(simulationManager))

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

    override def updateRailwaySchedule(update: (
        StationManager,
        RouteManager,
        TrainManager,
        TimetableManager
    ) => (StationManager, RouteManager, TrainManager, TimetableManager)): AppState =
      val (newStation, newRoute, newTrain, newTimetable) =
        update(stationManager, routeManager, trainManager, timetableManager)
      copy(newStation, newRoute, newTrain, timetableManager = newTimetable)

    override def initSimulation(update: (
        SimulationManager,
        StationManager,
        RouteManager,
        TrainManager,
        TimetableManager
    ) => SimulationManager): AppState =
      copy(simulationManager = update(simulationManager, stationManager, routeManager, trainManager, timetableManager))
