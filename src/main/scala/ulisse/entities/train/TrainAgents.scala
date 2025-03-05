package ulisse.entities.train

import ulisse.entities.route.Routes.RouteType
import ulisse.entities.simulation.agents.Perceptions.{Perception, PerceptionData}
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.railwayEnvironment.RailwayEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.TrainAgents.TrainAgent.TrainStates

import java.time.zone.ZoneOffsetTransitionRule

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
    def state: TrainStates.StateBehavior
    def distanceTravelled: Double
    def currentSpeed: Double
    def currentAcceleration: Double
    def motionData: TrainStates.MotionData
    // cane be called in a different way: what the need situation i wanna reset
    def resetDistanceTravelled: TrainAgent
    def updateDistanceTravelled(distanceDelta: Double): TrainAgent

  object TrainAgent:
    /** Creates a [[TrainAgent]] for a given `train` with default [[Stopped]] state and no distance travelled. */
    def apply(train: Train): TrainAgent =
      TrainAgentImpl(train, TrainStates.Stopped(0.0))

    private final case class TrainAgentImpl(train: Train, state: TrainStates.StateBehavior)
        extends TrainAgent:
      export train.*
      export state.motionData
      export state.motionData.{acceleration => currentAcceleration, distanceTravelled, speed => currentSpeed}
      override def resetDistanceTravelled: TrainAgent = TrainAgentImpl(train, state.reset())
      override def updateDistanceTravelled(distanceDelta: Double): TrainAgent =
        TrainAgentImpl(train, state.withOffsetDistance(distanceDelta))
      override def doStep(dt: Int, simulationEnvironment: RailwayEnvironment): TrainAgent =
        import ulisse.entities.simulation.environments.railwayEnvironment.PerceptionProvider.given
        val perception: Option[TrainAgentPerception[?]] = simulationEnvironment.perceptionFor[TrainAgent](this)
        copy(state = state.next(this, dt, perception))

    object TrainStates:
      private type Percepts = Option[TrainAgentPerception[?]]

      case class MotionData(distanceTravelled: Double, speed: Double, acceleration: Double):
        def withAcceleration(acc: Double): MotionData = copy(acceleration = acc)
        def withDistance(v: Double): MotionData       = copy(distanceTravelled = v)

      extension (motionData: MotionData)
        def updated(dt: Double): MotionData =
          val newSpeed = motionData.speed + motionData.acceleration * dt
          val newDistanceTravelled =
            motionData.distanceTravelled + newSpeed * dt + 0.5 * motionData.acceleration * Math.pow(dt, 2)
          motionData.copy(distanceTravelled = newDistanceTravelled, speed = newSpeed)

      private def emptyMotionData: MotionData = MotionData(0.0, 0.0, 0.0)

      sealed trait StateBehavior(val motionData: MotionData):
        def stateName: String
        // Resets motion data returning [[Stopped]] state with zeroed speed, acceleration and distance travelled values. */
        def reset(): StateBehavior = Stopped(emptyMotionData.distanceTravelled)
        def withOffsetDistance(offset: Double): StateBehavior =
          def positiveVal(base: Double, offset: Double) =
            if base + offset >= 0 then base + offset else 0
          this match
            case Stopped(currentDistance) => Stopped(positiveVal(currentDistance, offset))
            case Running(md)              => Running(md.withDistance(positiveVal(md.distanceTravelled, offset)))
            case Accelerating(md)         => Accelerating(md.withDistance(positiveVal(md.distanceTravelled, offset)))

        // ** Returns next state depending on current state, train of `agent`, `dt`, percepts `p`. */
        def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior
//        def updatedWith(f: MotionData => StateBehavior): StateBehavior

      final case class Stopped(currentDistance: Double)
          extends StateBehavior(emptyMotionData.withDistance(currentDistance)):
        override def stateName: String = "Stopped"
        override def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior =
          // TODO: define how calculate space between
          def enoughSpace(d: Option[Double]): Boolean = true
          val trainAcc                                = agent.techType.acceleration
          def accelerate(): StateBehavior             = Accelerating(motionData.withAcceleration(trainAcc).updated(dt))
          p.map {
            // train stopped on the route
            // train can accelerate if distance from ahead train is enough
            case TrainPerceptionInRoute(p) if enoughSpace(p.trainAheadDistance) => accelerate()
            // train stopped in station and depart
            case TrainPerceptionInStation(p) if p.hasToMove && p.routeTrackIsFree => accelerate()
          }.getOrElse(this)

      final case class Accelerating(md: MotionData) extends StateBehavior(md):
        override def stateName: String = "Accelerating"
        override def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior =
          Running(motionData.withAcceleration(3.0))

      final case class Running(md: MotionData) extends StateBehavior(md):
        override def stateName: String = "Running"
        override def next(agent: TrainAgent, dt: Int, p: Percepts): StateBehavior =
          Running(md.copy(distanceTravelled = md.distanceTravelled + 1))
