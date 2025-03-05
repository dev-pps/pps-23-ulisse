package ulisse.entities.simulation.data

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class EngineTest extends AnyWordSpec with Matchers:

  private val running       = true
  private val configuration = EngineConfiguration.defaultBatch()
  private val state         = EngineState(Some(1.0), 2.0, 3.0)

  "Engine" when:
    "created" should:
      "have a running flag, a configuration and a state" in:
        val engine = Engine(running, configuration, state)
        engine.running shouldBe running
        engine.configuration shouldBe configuration
        engine.state shouldBe state

    "created empty" should:
      "not be running, and have empty configuration and state" in:
        val engine = Engine.empty()
        engine.running shouldBe false
        engine.configuration shouldBe EngineConfiguration.empty()
        engine.state shouldBe EngineState.empty()

    "created empty with configuration" should:
      "not be running, and have the given configuration and empty state" in:
        val engine = Engine.emptyWithConfiguration(configuration)
        engine.running shouldBe false
        engine.configuration shouldBe configuration
        engine.state shouldBe EngineState.empty()

    "running flag is set" should:
      "set the running flag" in:
        val engine = Engine.empty().running = running
        engine.running shouldBe running

    "configuration is set" should:
      "set the configuration" in:
        val engine = Engine.empty().configuration = configuration
        engine.configuration shouldBe configuration

    "state is set" should:
      "set the state" in:
        val engine = Engine.empty().state = state
        engine.state shouldBe state
