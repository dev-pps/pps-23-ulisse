package ulisse.entities.simulation

import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.simulation.Simulations.Actions.SimulationAction
import ulisse.entities.train.Trains.Train

object Agents:
  trait SimulationAgent:
    def doStep(dt: Int, simulationEnvironment: SimulationEnvironment): SimulationAction

  trait TrainAgent extends Train with SimulationAgent:
    def travelDistance: Double
    def travelDistance_=(newTravelDistance: Double): TrainAgent
    def updateTravelDistance(distanceDelta: Double): TrainAgent
