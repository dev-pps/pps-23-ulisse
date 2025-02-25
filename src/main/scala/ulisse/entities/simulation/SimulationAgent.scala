package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.{Perception, RailwayEnvironment}
import ulisse.entities.simulation.Simulations.Actions.SimulationAction
import ulisse.entities.train.Trains.Train

trait SimulationAgent:
  type P <: Perception
  def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[SimulationAction]
