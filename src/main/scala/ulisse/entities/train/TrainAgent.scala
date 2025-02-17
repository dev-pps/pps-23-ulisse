package ulisse.entities.train

import ulisse.entities.simulation.Agents.SimulationAgent
import ulisse.entities.train.Trains.Train

trait TrainAgent extends Train with SimulationAgent:
  def travelDistance: Double
  def travelDistance_=(newTravelDistance: Double): TrainAgent
  def updateTravelDistance(distanceDelta: Double): TrainAgent
