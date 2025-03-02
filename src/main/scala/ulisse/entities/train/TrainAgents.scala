package ulisse.entities.train

import ulisse.entities.route.Routes.RouteType
import ulisse.entities.simulation.agents.Perceptions.{Perception, PerceptionData}
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.Environment
import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProvider.given_PerceptionProvider_RailwayEnvironment_TrainAgent
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.train.Trains.Train

object TrainAgents:
  trait TrainAgentInfo:
    def train: TrainAgent
    def timetables: Seq[DynamicTimetable]
  object TrainAgentInfo:
    def apply(train: TrainAgent, timetables: Seq[DynamicTimetable]): TrainAgentInfo =
      TrainAgentInfoImpl(train, timetables.filter(_.train == train))
    private final case class TrainAgentInfoImpl(train: TrainAgent, timetables: Seq[DynamicTimetable])
        extends TrainAgentInfo
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
    def routeTypology: RouteType
    def routeLength: Double
    def trainAheadDistance: Option[Double]
    def arrivalStationIsFree: Boolean

  object TrainRouteInfo:
    def apply(
        routeTypology: RouteType,
        routeLength: Double,
        trainAheadDistance: Option[Double],
        arrivalStationIsFree: Boolean
    ): TrainRouteInfo = TrainRouteInfoImpl(routeTypology, routeLength, trainAheadDistance, arrivalStationIsFree)

    private final case class TrainRouteInfoImpl(
        routeTypology: RouteType,
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
    def apply(train: Train): TrainAgent = TrainAgentImpl(train, 0.0, 0.0)

    private final case class TrainAgentImpl(train: Train, distanceTravelled: Double, currentSpeed: Double)
        extends TrainAgent:
      export train.*
      def distanceTravelled_=(newDistanceTravelled: Double): TrainAgent =
        val minDistanceTravelled = 0.0
        copy(distanceTravelled = math.max(minDistanceTravelled, newDistanceTravelled))

      // todo: modificare il tipo di ritorno con TrainAgent invece di `Option[Actions.SimulationAction]`
      override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): Option[TrainAgent] =
        val perception: Option[TrainAgentPerception[?]] = simulationEnvironment.perceptionFor[TrainAgent](this)
        // TODO: aggiornare anche env
        // in env la prima cosa che fa dopo aver fatto do step è aggiornare l'agente con l'azione nuova
        // --> diventerebbe che viene sostituito direttamente con l'agente restituito da doStep
        // TODO: here write behaviour of train based on perceptions.
        None
