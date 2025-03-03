package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.{Environment, RailwayEnvironment}
import ulisse.entities.train.Trains.Train

trait SimulationAgent[SA <: SimulationAgent[SA]]:
  self: SA =>
  type E <: Environment[E]
  def doStep(dt: Int, simulationEnvironment: E): Option[SA]
