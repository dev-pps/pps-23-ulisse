package ulisse.applications

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.AppState.Managers
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager

import scala.compiletime.{erasedValue, summonInline}
import scala.reflect.ClassTag

class AppStateTest extends AnyFlatSpec with Matchers:
  private val appState = AppState()

  "update manager" should "update a single manager" in:
    val stationManager  = mock[StationManager]
    val updatedAppState = appState.updateManager(stationManager)

    appState must not be updatedAppState
    updatedAppState.stationManager mustBe stationManager

  "update non-existing manager" should "not update the application state" in:
    val updatedAppState = appState.updateManager(mock[Managers])
    appState mustBe updatedAppState

  "update multiple managers" should "update all managers" in:
    val stationManager  = mock[StationManager]
    val routeManager    = mock[RouteManager]
    val updatedAppState = appState.updateManagers(stationManager, routeManager)

    appState must not be updatedAppState
    updatedAppState.stationManager mustBe stationManager
    updatedAppState.routeManager mustBe routeManager
