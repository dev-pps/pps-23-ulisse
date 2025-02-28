package ulisse.applications

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import ulisse.applications.managers.StationManager
import ulisse.entities.station.Station

class AppStateTest extends AnyFlatSpec with Matchers:
  private val appState = AppState()

  "AppState" should "update a single manager" in:
    val updateManager  = mock[StationManager => StationManager]
    val stationManager = mock[StationManager]
    val station        = mock[Station]

    when(updateManager(any[StationManager])).thenReturn(stationManager)
    when(stationManager.stations).thenReturn(List(station))

    val updatedAppState = appState.updateSingleManager(updateManager)
    appState must not be updatedAppState
    updatedAppState.stationManager.stations must be(List(station))
