package ulisse

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.must.Matchers.mustBe
import org.scalatest.matchers.should.Matchers
import ulisse.applications.AppState
import ulisse.dsl.RailwayDsl.CreateStation._
import ulisse.dsl.RailwayDsl.CreateStation
import ulisse.entities.Coordinate
import ulisse.entities.station.Station

class RailwayDslTest extends AnyFlatSpec with Matchers:
  private val appState = AppState()

  "create station with dsl" should "create a station" in:
    val stationTest = Station("test", Coordinate(0, 0), 1)
    val station     = CreateStation -> "test" at (0, 0) platform 1
    stationTest mustBe station
