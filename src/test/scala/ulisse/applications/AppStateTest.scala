package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.{SimulationManager, StationManager}
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager

import scala.compiletime.{erasedValue, summonInline}
import scala.reflect.ClassTag

object AppStateTest:
  val stationManager: StationManager       = mock[StationManager]
  val routeManager: RouteManager           = mock[RouteManager]
  val trainManager: TrainManager           = mock[TrainManager]
  val timetableManager: TimetableManager   = mock[TimetableManager]
  val simulationManager: SimulationManager = mock[SimulationManager]

  val updateStation: StationManager => StationManager       = _ => stationManager
  val updateTrain: TrainManager => TrainManager             = _ => trainManager
  val updateTimetable: TimetableManager => TimetableManager = _ => timetableManager
  val updateRailwayNetwork: (StationManager, RouteManager) => (StationManager, RouteManager) =
    (_, _) => (stationManager, routeManager)
  val updateStationSchedule
      : (StationManager, RouteManager, TimetableManager) => (StationManager, RouteManager, TimetableManager) =
    (_, _, _) => (stationManager, routeManager, timetableManager)
  val updateRouteSchedule: (RouteManager, TimetableManager) => (RouteManager, TimetableManager) =
    (_, _) => (routeManager, timetableManager)
  val updateTrainSchedule: (TrainManager, TimetableManager) => (TrainManager, TimetableManager) =
    (_, _) => (trainManager, timetableManager)
  val updateRailway: (StationManager, RouteManager, TrainManager) => (StationManager, RouteManager, TrainManager) =
    (_, _, _) => (stationManager, routeManager, trainManager)
  val updateSimulation: (SimulationManager, StationManager) => SimulationManager = (_, _) => simulationManager

class AppStateTest extends AnyFlatSpec with Matchers:
  import AppStateTest.*
  private val appState = AppState()

  "update station manager" should "update manager" in:
    val newState = appState.updateStation(updateStation)
    newState.stationManager mustBe stationManager

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
    val newState = appState.updateRailway(updateRailway)

    newState.stationManager mustBe stationManager
    newState.routeManager mustBe routeManager
    newState.trainManager mustBe trainManager

  "update simulation manager" should "update manager" in:
    val newState = appState.updateSimulation(updateSimulation)

    newState.simulationManager mustBe simulationManager
