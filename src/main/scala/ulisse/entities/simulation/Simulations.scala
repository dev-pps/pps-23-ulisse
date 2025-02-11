package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.SimulationEnvironment

object Simulations:
  final case class SimulationData(step: Int, secondElapsed: Double, simulationEnvironment: SimulationEnvironment)
