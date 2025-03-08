package ulisse.entities.simulation.agents

import ulisse.entities.simulation.environments.Environments.EnvironmentsCoordinator

/** Basic SimulationAgent trait. */
trait SimulationAgent[SA <: SimulationAgent[SA]]:
  self: SA =>

  /** Environment type. */
  type EC <: EnvironmentsCoordinator[EC]

  /** Do a step in the simulation. */
  def doStep(dt: Int, environment: EC): SA
