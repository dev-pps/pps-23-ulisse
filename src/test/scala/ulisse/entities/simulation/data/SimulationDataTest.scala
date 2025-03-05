package ulisse.entities.simulation.data

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import ulisse.entities.simulation.environments.railwayEnvironment.ConfigurationDataTest.simpleConfigurationData
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.Time

class SimulationDataTest extends AnyWordSpec with Matchers:
  private val step                    = 10
  private val millisecondsElapsed     = 2.t
  private val railwayEnvironment      = RailwayEnvironment(Time(10, 0, 0), simpleConfigurationData)
  private val otherRailwayEnvironment = RailwayEnvironment.auto(simpleConfigurationData)
  private val simulationData          = SimulationData(step, millisecondsElapsed, railwayEnvironment)

  "Simulation data" when:
    "created" should:
      "have step, second elapsed and simulation environment" in:
        simulationData.step shouldBe step
        simulationData.millisecondsElapsed shouldBe millisecondsElapsed
        simulationData.simulationEnvironment shouldBe railwayEnvironment

      "doesn't allow negative values" in:
        SimulationData(-1, -2, railwayEnvironment) shouldBe SimulationData.withEnvironment(railwayEnvironment)

    "created with environment" should:
      "have no step and second elapsed" in:
        val simulationData = SimulationData.withEnvironment(railwayEnvironment)
        simulationData.step shouldBe 0
        simulationData.millisecondsElapsed shouldBe 0
        simulationData.simulationEnvironment shouldBe railwayEnvironment

    "created empty" should:
      "have no step and no second elapsed" in:
        SimulationData.empty() shouldBe SimulationData.withEnvironment(RailwayEnvironment.empty())

  "step is increased by one" should:
    "increase step by one" in:
      simulationData.increaseStepByOne().step shouldBe step + 1

  "second elapsed is increased by delta" should:
    "increase second elapsed by delta" in:
      val delta = 5.0
      simulationData.increaseMillisecondsElapsedBy(delta).millisecondsElapsed shouldBe millisecondsElapsed + delta

  "new environment is set" should:
    "set new environment" in:
      val newSimulationData = simulationData.simulationEnvironment = otherRailwayEnvironment
      newSimulationData.simulationEnvironment shouldBe otherRailwayEnvironment

  "reset" should:
    "reset simulation data" in:
      val newSimulationData =
        simulationData.increaseStepByOne().increaseMillisecondsElapsedBy(10).simulationEnvironment_=(
          otherRailwayEnvironment
        ).reset()
      newSimulationData shouldBe SimulationData.withEnvironment(railwayEnvironment)
