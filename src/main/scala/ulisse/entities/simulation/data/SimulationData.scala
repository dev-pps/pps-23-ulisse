package ulisse.entities.simulation.data

import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.utils.Times.Time

trait SimulationData:
  def step: Int
  def secondElapsed: Double
  def simulationEnvironment: RailwayEnvironment
  def increaseStepByOne(): SimulationData
  def increaseSecondElapsedBy(delta: Double): SimulationData
  def reset(): SimulationData

object SimulationData:
  def apply(step: Int, secondElapsed: Double, simulationEnvironment: RailwayEnvironment): SimulationData =
    SimulationDataImpl(step, secondElapsed, simulationEnvironment, simulationEnvironment)
  def withEnvironment(environment: RailwayEnvironment): SimulationData =
    SimulationData(0, 0, environment)
  def empty(): SimulationData = withEnvironment(RailwayEnvironment.empty())

  extension (simulationData: SimulationData)
    def cumulativeDelay: Time =
      Time.secondsToOverflowTime(
        simulationData.simulationEnvironment
          .timetables
          .flatMap(_.currentDelay)
          .map(_.toSeconds).sum
      )

private final case class SimulationDataImpl(
    step: Int,
    secondElapsed: Double,
    initialSimulationEnvironment: RailwayEnvironment,
    simulationEnvironment: RailwayEnvironment
) extends SimulationData:
  override def increaseStepByOne(): SimulationData                    = copy(step = step + 1)
  override def increaseSecondElapsedBy(delta: Double): SimulationData = copy(secondElapsed = secondElapsed + delta)
  override def reset(): SimulationData = SimulationData.withEnvironment(initialSimulationEnvironment)
