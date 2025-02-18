package ulisse.entities.train

import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.train.Trains.Train

trait TrainAgent extends Train with SimulationAgent:
  def distanceTravelled: Double
  def distanceTravelled_=(newTravelDistance: Double): TrainAgent
  def resetDistanceTravelled(): TrainAgent                       = distanceTravelled = 0
  def updateDistanceTravelled(distanceDelta: Double): TrainAgent = distanceTravelled += distanceDelta
