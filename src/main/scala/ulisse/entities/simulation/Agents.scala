package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.Actions.SimulationAction

object Agents:
  trait SimulationAgent:
    def doStep(dt: Int, simulationEnvironment: SimulationEnvironment): SimulationAction

  trait TrainAgent extends SimulationAgent:
    def travelDistance: Double
    def updateTravelDistance(distanceDelta: Double): TrainAgent
