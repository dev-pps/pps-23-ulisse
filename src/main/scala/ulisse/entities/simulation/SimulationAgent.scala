package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.Actions.SimulationAction
import ulisse.entities.train.Trains.Train

trait SimulationAgent:
  def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[SimulationAction]
