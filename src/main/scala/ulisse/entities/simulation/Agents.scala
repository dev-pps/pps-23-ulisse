package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.Actions.SimulationAction
import ulisse.entities.train.Trains.Train

object Agents:
  trait SimulationAgent:
    def doStep(dt: Int, simulationEnvironment: SimulationEnvironment): SimulationAction
