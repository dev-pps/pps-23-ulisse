package ulisse.entities.simulation.data

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.data.EngineConfiguration.{defaultCyclesPerSecond, defaultStepSize}

class EngineConfigurationTest extends AnyWordSpec with Matchers:
  private val stepSize            = 10
  private val cyclesPerSecond     = 2
  private val engineConfiguration = EngineConfiguration(stepSize, Some(cyclesPerSecond))

  "EngineConfiguration" when:
    "created" should:
      "have step size and cyclesPerSecond" in:
        engineConfiguration.stepSize shouldBe stepSize
        engineConfiguration.cyclesPerSecond shouldBe Some(cyclesPerSecond)

      "doesn't allow negative values" in:
        EngineConfiguration(-1, Some(0)) shouldBe EngineConfiguration.empty()

    "created checked" should:
      "have step size and cycles per second" in:
        val engineConfiguration = EngineConfiguration.createCheckedConfiguration(stepSize, Some(cyclesPerSecond))
        engineConfiguration shouldBe Some(EngineConfiguration(stepSize, Some(cyclesPerSecond)))

      "is not created if stepSize are negative" in:
        val engineConfiguration = EngineConfiguration.createCheckedConfiguration(-1, Some(cyclesPerSecond))
        engineConfiguration shouldBe None

      "is not created if cycles per second are non-positive" in:
        val engineConfiguration = EngineConfiguration.createCheckedConfiguration(stepSize, Some(0))
        engineConfiguration shouldBe None

    "created with cycles per seconds" should:
      "have default step size" in:
        val engineConfiguration = EngineConfiguration.withCps(cyclesPerSecond)
        engineConfiguration.stepSize shouldBe defaultStepSize
        engineConfiguration.cyclesPerSecond shouldBe Some(cyclesPerSecond)

      "have default cycles per second" in:
        val engineConfiguration = EngineConfiguration.withCps(0)
        engineConfiguration.cyclesPerSecond shouldBe None

    "default batch" should:
      "have default step size and no cycles per second" in:
        val engineConfiguration = EngineConfiguration.defaultBatch()
        engineConfiguration.stepSize shouldBe defaultStepSize
        engineConfiguration.cyclesPerSecond shouldBe None

    "default timed" should:
      "have default step size and default cycles per second" in:
        val engineConfiguration = EngineConfiguration.defaultTimed()
        engineConfiguration.stepSize shouldBe defaultStepSize
        engineConfiguration.cyclesPerSecond shouldBe Some(defaultCyclesPerSecond)

    "empty" should:
      "have no step size and no cycles per second" in:
        val engineConfiguration = EngineConfiguration.empty()
        engineConfiguration.stepSize shouldBe 0
        engineConfiguration.cyclesPerSecond shouldBe None
