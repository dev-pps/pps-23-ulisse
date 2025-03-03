package ulisse.entities.train

import ulisse.entities.route.Routes.TypeRoute
import ulisse.entities.train.Trains.Train
import ulisse.entities.simulation.agents.Perceptions.{Perception, PerceptionData}
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.Environment
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProvider.given_PerceptionProvider_RailwayEnvironment_TrainAgent

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
  trait TrainAgentPerception[PD <: PerceptionData]                      extends Perception[PD]
  case class TrainPerceptionInStation(perceptionData: TrainStationInfo) extends TrainAgentPerception[TrainStationInfo]
  case class TrainPerceptionInRoute(perceptionData: TrainRouteInfo)     extends TrainAgentPerception[TrainRouteInfo]

  trait TrainAgent extends Train with SimulationAgent[TrainAgent]:
    override type E = RailwayEnvironment
    def distanceTravelled: Double
    def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent
    def resetDistanceTravelled(): TrainAgent                       = distanceTravelled = (0)
    def updateDistanceTravelled(distanceDelta: Double): TrainAgent = distanceTravelled += distanceDelta

  object TrainAgent:
    def apply(train: Train): TrainAgent = TrainAgentImpl(train, 0.0)

    private final case class TrainAgentImpl(train: Train, distanceTravelled: Double) extends TrainAgent:
      export train.*
      def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
        val minDistanceTravelled = 0.0
        copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))
      override def doStep(dt: Int, simulationEnvironment: E): Option[TrainAgent] =
        val perception: Option[TrainAgentPerception[?]] = simulationEnvironment.perceptionFor[TrainAgent](this)
        Some(this)
