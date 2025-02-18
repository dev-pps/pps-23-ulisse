package ulisse.entities.train

import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.Trains.Train

trait TrainAgent extends Train with SimulationAgent:
  def distanceTravelled: Double
  def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent
  def resetDistanceTravelled(): TrainAgent                       = distanceTravelled = 0
  def updateDistanceTravelled(distanceDelta: Double): TrainAgent = distanceTravelled += distanceDelta
  def isOnRoute: Boolean                                         = distanceTravelled > 0

object TrainAgent:
  def createTrainAgent(train: Train): TrainAgent = TrainAgentImpl(train, 0.0)
  private final case class TrainAgentImpl(train: Train, distanceTravelled: Double) extends TrainAgent:
    export train.*
    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent = copy(distanceTravelled = newDistanceTravelled)

    override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[Actions.SimulationAction] = None
