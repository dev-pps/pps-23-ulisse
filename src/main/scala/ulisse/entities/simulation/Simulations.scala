package ulisse.entities.simulation

import cats.{Functor, Monad}
import ulisse.entities.simulation.Environments.SimulationEnvironment

object Simulations:
  final case class EngineData(
      running: Boolean,
      cyclesPerSecond: Option[Double],
      lastUpdate: Option[Double],
      lastDelta: Double,
      elapsedCycleTime: Double
  ):
    def update(currentUpdate: Double): EngineData =
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

  final case class SimulationData(
      step: Int,
      secondElapsed: Double,
      simulationEnvironment: SimulationEnvironment
  ):
    def clear(): SimulationData =
      copy(step = 0, secondElapsed = 0, simulationEnvironment = SimulationEnvironment.empty())
