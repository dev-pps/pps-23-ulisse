package ulisse.entities.train

import ulisse.entities.route.Routes.TypeRoute
import ulisse.entities.simulation.Environments.{Perception, RailwayEnvironment}
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.train.Trains.Train

object TrainAgents:
  trait TrainAgentPerceptionData

  trait TrainStationInfo extends TrainAgentPerceptionData:
    def hasToMove: Boolean
    def routeTrackIsFree: Boolean

  trait TrainRouteInfo extends TrainAgentPerceptionData:
    def routeTypology: TypeRoute
    def routeLength: Double
    def trainAheadDistance: Option[Double]
    def arrivalStationIsFree: Boolean

  trait TrainAgentPerception extends Perception:
    def perceptionData: TrainAgentPerceptionData

  trait TrainAgent extends Train with SimulationAgent:
    def distanceTravelled: Double
    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent
    def resetDistanceTravelled(): TrainAgent                       = distanceTravelled = (0)
    def updateDistanceTravelled(distanceDelta: Double): TrainAgent = distanceTravelled += distanceDelta
    // TODO evaluate remove this method
    def isOnRoute: Boolean                       = distanceTravelled > 0
    def matchId(otherTrain: TrainAgent): Boolean = name == otherTrain.name
  object TrainAgent:
    def apply(train: Train): TrainAgent = TrainAgentImpl(train, 0.0)

    private final case class TrainAgentImpl(train: Train, distanceTravelled: Double) extends TrainAgent:
      export train.*
      def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
        val minDistanceTravelled = 0.0
        copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))
      override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[Actions.SimulationAction] = None
