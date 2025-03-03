package ulisse.entities.simulation.data

import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment

object SimulationData:
  def empty(): SimulationData = SimulationData(0, 0, RailwayEnvironment.empty(), RailwayEnvironment.empty())
  extension (simulationData: SimulationData)
    def increaseStepByOne(): SimulationData = simulationData.copy(step = simulationData.step + 1)
    def increaseSecondElapsedBy(delta: Double): SimulationData =
      simulationData.copy(secondElapsed = simulationData.secondElapsed + delta)

final case class SimulationData(
    step: Int,
    secondElapsed: Double,
    private val initialSimulationEnvironment: RailwayEnvironment,
    simulationEnvironment: RailwayEnvironment
):
  def withEnvironment(environment: RailwayEnvironment): SimulationData =
    copy(initialSimulationEnvironment = environment, simulationEnvironment = environment)
  def reset(): SimulationData =
    copy(step = 0, secondElapsed = 0, simulationEnvironment = initialSimulationEnvironment)
