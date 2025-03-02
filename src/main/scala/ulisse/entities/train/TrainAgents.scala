package ulisse.entities.train

import ulisse.entities.route.Routes.TypeRoute
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.Actions
import ulisse.entities.simulation.{Environments, SimulationAgent}
import ulisse.entities.train.Trains.Train
import ulisse.entities.simulation.Environments.RailwayEnvironment.*
import ulisse.entities.simulation.Environments.RailwayEnvironment.given
import ulisse.entities.simulation.Perceptions.{Perception, PerceptionData}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.Timetable

object TrainAgents:
  case class TrainAgentInfo(train: TrainAgent, timetables: List[DynamicTimetable])
  sealed trait TrainAgentPerceptionData extends PerceptionData

  sealed trait TrainStationInfo extends TrainAgentPerceptionData:
    def hasToMove: Boolean
    def routeTrackIsFree: Boolean

  object TrainStationInfo:
    def apply(hasToMove: Boolean, routeTrackIsFree: Boolean): TrainStationInfo =
      TrainStationInfoImpl(hasToMove, routeTrackIsFree)

    private final case class TrainStationInfoImpl(hasToMove: Boolean, routeTrackIsFree: Boolean)
        extends TrainStationInfo

  sealed trait TrainRouteInfo extends TrainAgentPerceptionData:
    def routeTypology: TypeRoute
    def routeLength: Double
    def trainAheadDistance: Option[Double]
    def arrivalStationIsFree: Boolean

  object TrainRouteInfo:
    def apply(
        routeTypology: TypeRoute,
        routeLength: Double,
        trainAheadDistance: Option[Double],
        arrivalStationIsFree: Boolean
    ): TrainRouteInfo = TrainRouteInfoImpl(routeTypology, routeLength, trainAheadDistance, arrivalStationIsFree)

    private final case class TrainRouteInfoImpl(
        routeTypology: TypeRoute,
        routeLength: Double,
        trainAheadDistance: Option[Double],
        arrivalStationIsFree: Boolean
    ) extends TrainRouteInfo
  trait TrainAgentPerception extends Perception[TrainAgentPerceptionData]

  trait TrainAgent extends Train with SimulationAgent:
    def distanceTravelled: Double
    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent
    def resetDistanceTravelled(): TrainAgent                       = distanceTravelled = (0)
    def updateDistanceTravelled(distanceDelta: Double): TrainAgent = distanceTravelled += distanceDelta
    def matchId(otherTrain: TrainAgent): Boolean                   = name == otherTrain.name

  object TrainAgent:
    def apply(train: Train): TrainAgent = TrainAgentImpl(train, 0.0)

    private final case class TrainAgentImpl(train: Train, distanceTravelled: Double) extends TrainAgent:
      export train.*
      def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
        val minDistanceTravelled = 0.0
        copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))
      override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[Actions.SimulationAction] =
        val perception: Option[TrainAgentPerception] = simulationEnvironment.perceptionFor[TrainAgent](this)
        None
