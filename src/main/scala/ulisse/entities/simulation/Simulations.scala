package ulisse.entities.simulation

import cats.{Functor, Monad}
import ulisse.entities.simulation.Environments.SimulationEnvironment

object Simulations:
  object EngineState:
    def empty(): EngineState = EngineState(false, None, None, 0, 0)
    extension (engineState: EngineState)
      def decreaseElapsedCycleTimeBy(delta: Double): EngineState =
        engineState.copy(elapsedCycleTime = engineState.elapsedCycleTime - delta)

  final case class EngineState(
      running: Boolean,
      cyclesPerSecond: Option[Int],
      lastUpdate: Option[Double],
      lastDelta: Double,
      elapsedCycleTime: Double
  ):
    def update(currentUpdate: Double): EngineState =
      lastUpdate match
        case Some(lastUpdate) =>
          val deltaElapsed = (currentUpdate - lastUpdate) / 1000.0
          copy(
            lastDelta = deltaElapsed,
            elapsedCycleTime = elapsedCycleTime + deltaElapsed,
            lastUpdate = Some(currentUpdate)
          )
        case None =>
          copy(lastUpdate = Some(currentUpdate))

  object SimulationData:
    def empty(): SimulationData = SimulationData(0, 0, SimulationEnvironment.empty())
    extension (simulationData: SimulationData)
      def increaseStepByOne(): SimulationData = simulationData.copy(step = simulationData.step + 1)
      def increaseSecondElapsedBy(delta: Double): SimulationData =
        simulationData.copy(secondElapsed = simulationData.secondElapsed + delta)

  final case class SimulationData(
      step: Int,
      secondElapsed: Double,
      simulationEnvironment: SimulationEnvironment
  ):
    def clear(): SimulationData =
      copy(step = 0, secondElapsed = 0, simulationEnvironment = SimulationEnvironment.empty())
