package ulisse

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import ulisse.applications.AppState
import ulisse.dsl.RailwayDsl.*

class RailwayDslTest extends AnyFlatSpec with Matchers:
  private val appState = AppState()

//  "create station" should "push a station in the app state" in:
//    appState
//
