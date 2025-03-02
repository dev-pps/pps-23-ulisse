package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.TechnologyManagers.TechnologyManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager
import ulisse.entities.train.Trains.TrainTechnology

import scala.compiletime.{erasedValue, summonInline}
import scala.reflect.ClassTag

object AppStateTest:
  val stationManager: StationManager                        = mock[StationManager]
  val routeManager: RouteManager                            = mock[RouteManager]
  val technologyManager: TechnologyManager[TrainTechnology] = mock[TechnologyManager[TrainTechnology]]
  val trainManager: TrainManager                            = mock[TrainManager]
  val timetableManager: TimetableManager                    = mock[TimetableManager]
  val simulationManager: SimulationManager                  = mock[SimulationManager]

  val updateStation: StationManager => StationManager = _ => stationManager
  val updateRoute: RouteManager => RouteManager       = _ => routeManager
  val updateTechnology: TechnologyManager[TrainTechnology] => TechnologyManager[TrainTechnology] =
    _ => technologyManager
  val updateTrain: (TrainManager, TechnologyManager[TrainTechnology]) => TrainManager = (_, _) => trainManager
  val updateTimetable: TimetableManager => TimetableManager                           = _ => timetableManager
  val updateSimulation: SimulationManager => SimulationManager                        = _ => simulationManager
  val updateRailwayNetwork: (StationManager, RouteManager) => (StationManager, RouteManager) =
    (_, _) => (stationManager, routeManager)
  val updateStationSchedule
      : (StationManager, RouteManager, TimetableManager) => (StationManager, RouteManager, TimetableManager) =
    (_, _, _) => (stationManager, routeManager, timetableManager)
  val updateRouteSchedule: (RouteManager, TimetableManager) => (RouteManager, TimetableManager) =
    (_, _) => (routeManager, timetableManager)
  val updateTrainSchedule: (TrainManager, TimetableManager) => (TrainManager, TimetableManager) =
    (_, _) => (trainManager, timetableManager)
  val updateRailwaySchedule: (StationManager, RouteManager, TrainManager, TimetableManager) => (
      StationManager,
      RouteManager,
      TrainManager,
      TimetableManager
  ) =
    (_, _, _, _) => (stationManager, routeManager, trainManager, timetableManager)
  val initSimulation: (SimulationManager, StationManager) => SimulationManager = (_, _) => simulationManager

class AppStateTest extends AnyFlatSpec with Matchers:
  import AppStateTest.*
  private val appState = AppState()

  "read station manager" should "be a function that not update manager" in:
    val initStationManager = appState.stationManager
    appState.readStation(_ => ())
    appState.stationManager mustBe initStationManager

  "read route manager" should "be a function that not update manager" in:
    val initRouteManager = appState.routeManager
    appState.readRoute(_ => ())
    appState.routeManager mustBe initRouteManager

  "read train manager" should "be a function that not update manager" in:
    val initTrainManager = appState.trainManager
    appState.readTrain((_, _) => ())
    appState.trainManager mustBe initTrainManager

  "read timetable manager" should "be a function that not update manager" in:
    val initTimetableManager = appState.timetableManager
    appState.readTimetable(_ => ())
    appState.timetableManager mustBe initTimetableManager

  "read simulation manager" should "be a function that not update manager" in:
    val initSimulationManager = appState.simulationManager
    appState.readSimulation(_ => ())
    appState.simulationManager mustBe initSimulationManager

  "update station manager" should "update manager" in:
    val newState = appState.updateStation(updateStation)
    newState.stationManager mustBe stationManager

  "update route manager" should "update manager" in:
    val newState = appState.updateRoute(updateRoute)
    newState.routeManager mustBe routeManager

  "update technology manager" should "update manager" in:
    val newState = appState.updateTechnology(updateTechnology)
    newState.technologyManager mustBe technologyManager

  "update train manager" should "update manager" in:
    val newState = appState.updateTrain(updateTrain)
    newState.trainManager mustBe trainManager

  "update timetable manager" should "update manager" in:
    val newState = appState.updateTimetable(updateTimetable)
    newState.timetableManager mustBe timetableManager

  "update railway network" should "update station and route managers" in:
    val newState = appState.updateRailwayNetwork(updateRailwayNetwork)

    newState.stationManager mustBe stationManager
    newState.routeManager mustBe routeManager

  "update station schedule" should "update station, route and timetable managers" in:
    val newState = appState.updateStationSchedule(updateStationSchedule)

    newState.stationManager mustBe stationManager
    newState.timetableManager mustBe timetableManager

  "update route schedule" should "update route and timetable managers" in:
    val newState = appState.updateRouteSchedule(updateRouteSchedule)

    newState.routeManager mustBe routeManager
    newState.timetableManager mustBe timetableManager

  "update train schedule" should "update train and timetable managers" in:
    val newState = appState.updateTrainSchedule(updateTrainSchedule)

    newState.trainManager mustBe trainManager
    newState.timetableManager mustBe timetableManager

  "update railway" should "update station, route and train managers" in:
    val newState = appState.updateRailwaySchedule(updateRailwaySchedule)

    newState.stationManager mustBe stationManager
    newState.routeManager mustBe routeManager
    newState.trainManager mustBe trainManager

  "update simulation manager" should "update manager" in:
    val newState = appState.updateSimulation(updateSimulation)

    newState.simulationManager mustBe simulationManager

  "init simulation manager" should "update manager" in:
    val newState = appState.initSimulation(initSimulation)

    newState.simulationManager mustBe simulationManager
