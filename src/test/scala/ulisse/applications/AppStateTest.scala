package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager
import ulisse.applications.managers.TrainManagers.TrainManager

import scala.compiletime.{erasedValue, summonInline}
import scala.reflect.ClassTag

class AppStateTest extends AnyFlatSpec with Matchers:
  private val appState = AppState()

  "update railway map" should "update the application state" in:
    val stationManager   = mock[StationManager]
    val routeManager     = mock[RouteManager]
    val timetableManager = mock[TimetableManager]

    val newState = appState.updateMap((_, _, _) => (stationManager, routeManager, timetableManager))

    newState.stationManager mustBe stationManager
    newState.routeManager mustBe routeManager
    newState.timetableManager mustBe timetableManager

  "update train" should "update the application state" in:
    val trainManager     = mock[TrainManager]
    val timetableManager = mock[TimetableManager]

    val newState = appState.updateTrain((_, _) => (trainManager, timetableManager))

    newState.trainManager mustBe trainManager
    newState.timetableManager mustBe timetableManager
