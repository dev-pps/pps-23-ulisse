package ulisse.entities.train

import ulisse.entities.route.Routes.RouteType
import ulisse.entities.simulation.agents.Perceptions.{Perception, PerceptionData}
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProviders.given_PerceptionProvider_RailwayEnvironment_TrainAgent
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.MotionDatas.{emptyMotionData, MotionData}
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates.StateBehavior
import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProviders.given_PerceptionProvider_RailwayEnvironment_TrainAgent

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
    override type EC = RailwayEnvironment
    def state: TrainStates.StateBehavior
    def motionData: MotionDatas.MotionData
    def distanceTravelled: Double
    // cane be called in a different way: what the need situation i wanna reset
    def resetDistanceTravelled: TrainAgent
    def updateDistanceTravelled(distanceDelta: Double): TrainAgent

  object TrainAgent:
    /** Creates a [[TrainAgent]] for a given `train` with default [[Stopped]] state and no distance travelled. */
    def apply(train: Train): TrainAgent =
      TrainAgentImpl(train, TrainStates.Stopped(emptyMotionData))
    def withInitialState(train: Train, state: StateBehavior): TrainAgent =
      TrainAgentImpl(train, state)

    private final case class TrainAgentImpl(train: Train, state: TrainStates.StateBehavior)
        extends TrainAgent:
      export train.*

      override def motionData: MotionData             = state.motionData
      override def resetDistanceTravelled: TrainAgent = TrainAgentImpl(train, state.reset())
      override def updateDistanceTravelled(distanceDelta: Double): TrainAgent =
        TrainAgentImpl(train, state.withOffsetDistance(distanceDelta))

      override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): TrainAgent =
        import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProviders.given
        val perception: Option[TrainAgentPerception[?]] = simulationEnvironment.perceptionFor[TrainAgent](this)
        copy(state = state.next(this, dt, perception))
//        updateDistanceTravelled(20)

      override def distanceTravelled: Double = state.motionData.distanceTravelled

    object TrainStates:
      private type Percepts = Option[TrainAgentPerception[?]]
      sealed trait StateBehavior:
        def motionData: MotionData
        def stateName: String
        // Resets motion data returning [[Stopped]] state with zeroed speed, acceleration and distance travelled values. */
        def reset(): StateBehavior = Stopped(emptyMotionData)
        def withOffsetDistance(offset: Double): StateBehavior = this match
          case Stopped(md) => Stopped(md.withDistanceOffset(offset))
          case Running(md) => Running(md.withDistanceOffset(offset))
        // ** Returns next state depending on current state, train of `agent`, `dt`, percepts `p`. */
        def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior

      private def enoughSpace(d: Option[Double]): Boolean = true
      private def shouldStop(ri: TrainRouteInfo): Boolean =
        !enoughSpace(ri.trainAheadDistance) || !ri.arrivalStationIsFree

      final case class Stopped(motionData: MotionData)
          extends StateBehavior:
        override def stateName: String = "Stopped"
        override def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior =
          p.map {
            // train on route have no train ahead or arrival station is free
            case TrainPerceptionInRoute(p) if !shouldStop(p) =>
              val speed = Math.min(p.routeTypology.technology.maxSpeed, agent.maxSpeed)
              Running(motionData.withSpeed(speed).updated(dt))
            // train stopped in station starts and run on route
            case TrainPerceptionInStation(p) if p.hasToMove && p.routeTrackIsFree =>
              Running(motionData.withSpeed(agent.maxSpeed).updated(dt))

            case _ => this
          }.getOrElse(this)

      final case class Running(motionData: MotionData) extends StateBehavior:
        override def stateName: String = "Running"
        override def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior =
          p.map {
            // train on route have a train ahead stops
            case TrainPerceptionInRoute(p) if !enoughSpace(p.trainAheadDistance) => Stopped(motionData)
            // train on route reach destination and can enter in free station then stops
            case TrainPerceptionInRoute(p) if motionData.distanceTravelled >= p.routeLength && p.arrivalStationIsFree =>
              Stopped(emptyMotionData)
            // train is moving in station can move and track is free update position
            case TrainPerceptionInStation(p) if p.hasToMove && p.routeTrackIsFree => Running(motionData.updated(dt))
            case _                                                                => this
          }.getOrElse(Running(motionData.updated(dt)))
