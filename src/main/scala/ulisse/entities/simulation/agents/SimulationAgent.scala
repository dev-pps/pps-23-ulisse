package ulisse.entities.simulation.agents

import ulisse.entities.simulation.environments.{Environment, EnvironmentsCoordinator}
import ulisse.entities.train.Trains.Train

/** Basic SimulationAgent trait */
trait SimulationAgent[SA <: SimulationAgent[SA]]:
  self: SA =>

  /** Environment type */
  type E <: EnvironmentsCoordinator[E]

  /** Do a step in the simulation */
  def doStep(dt: Int, simulationEnvironment: E): SA
