package ulisse.entities.simulation.data

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.data.EngineState.{defaultElapsedCycleTime, defaultLastDelta, defaultLastUpdate}

class EngineStateTest extends AnyWordSpec with Matchers:
  private val lastUpdate = 1.0
  private val lastDelta = 2.0
  private val elapsedCycleTime = 3.0
  private val engineState = EngineState(Some(lastUpdate), lastDelta, elapsedCycleTime)

  "Engine state" when:
    "created" should:
      "have last update, last delta and elapsed cycle time" in:
        engineState.lastUpdate shouldBe Some(lastUpdate)
        engineState.lastDelta shouldBe lastDelta
        engineState.elapsedCycleTime shouldBe elapsedCycleTime

      "doesn't allow negative values" in:
        val engineState = EngineState(Some(-1.0), -2.0, -3.0)
        engineState.lastUpdate shouldBe defaultLastUpdate
        engineState.lastDelta shouldBe defaultLastDelta
        engineState.elapsedCycleTime shouldBe defaultElapsedCycleTime

    "created empty" should:
      "have default values" in:
        val engineState = EngineState.empty()
        engineState.lastUpdate shouldBe defaultLastUpdate
        engineState.lastDelta shouldBe defaultLastDelta
        engineState.elapsedCycleTime shouldBe defaultElapsedCycleTime
    
    "elapsed cycle time is updated" should:
      "update elapsed cycle time" in:
        val delta = 5.0
        engineState.updateElapsedCycleTime(delta).elapsedCycleTime shouldBe elapsedCycleTime + delta

    "updated" should:
      "set last update when is none" in:
        val currentUpdate = 6.0
        val engineState = EngineState.empty()
        engineState.update(currentUpdate).lastUpdate shouldBe Some(currentUpdate)

      "set last update when current update is greater or equal than last update" in:
        val currentUpdate = lastUpdate + 1
        engineState.update(currentUpdate).lastUpdate shouldBe Some(currentUpdate)

      "update lastDelta when current update is greater or equal than last update" in:
        val deltaElapsed = 1.0
        val currentUpdate = lastUpdate + deltaElapsed
        engineState.update(currentUpdate).lastDelta shouldBe deltaElapsed

      "update elapsed cycle time when current update is greater or equal than last update" in:
        val deltaElapsed = 1.0
        val currentUpdate = lastUpdate + deltaElapsed
        engineState.update(currentUpdate).elapsedCycleTime shouldBe elapsedCycleTime + deltaElapsed

      "do nothing if current update is less than last update" in:
        val currentUpdate = lastUpdate - 1
        engineState.update(currentUpdate) shouldBe engineState
