package ulisse.entities.train

import ulisse.entities.train.MotionDatas.MotionData
import ulisse.entities.train.TrainAgentPerceptions.*
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.Train

/** Containing definition of [[StateBehavior]] concept for a TrainAgent and
  * all specific states like [[Stopped]] and[[Running]].
  */
object TrainAgentStates:
  private type Percepts = Option[TrainAgentPerception[?]]

  /** Behaviour of train */
  abstract class StateBehavior:
    /** Returns [[MotionData]] of train in the current StateBehaviour. */
    def motionData: MotionData

    /** Returns current StateBehavior name. */
    def stateName: String
    def enoughSpace(d: Option[Double], train: Train): Boolean
    def shouldStop(ri: TrainRouteInfo, train: Train): Boolean =
      !enoughSpace(ri.trainAheadDistance, train) || !ri.arrivalStationIsFree

    /** Resets motion data returning [[Stopped]] state with zeroed speed, acceleration and distance travelled values. */
    def reset(): StateBehavior = Stopped(MotionDatas.emptyMotionData)

    /** Returns new StateBehavior with the given `offset` applied to MotionData distance. */
    def withOffsetDistance(offset: Double): StateBehavior = this match
      case Stopped(md) => Stopped(md.withDistanceOffset(offset))
      case Running(md) => Running(md.withDistanceOffset(offset))

    /** Returns next state depending on current state, train of `agent`, `dt`, percepts `p`. */
    def next(train: Train, dt: Int, p: Percepts): StateBehavior

  /** Mixin trait that add capacity to know if there is enough space ahead of train. */
  trait BasicSpaceManagement extends StateBehavior:
    private val defaultSpaceLimit = 20
    def enoughSpace(d: Option[Double], train: Train): Boolean =
      d.forall { availableSpace => availableSpace >= defaultSpaceLimit }

  /** Stopped state Behavior where train agent is not moving (speed and acceleration are zero).
    * It skips to next state [[Running]] when there are condition to move safely from station to route and vice versa.
    */
  final case class Stopped(motionData: MotionData)
      extends StateBehavior with BasicSpaceManagement:
    override def stateName: String = "Stopped"

    override def next(train: Train, dt: Int, p: Percepts): StateBehavior =
      p.map {
        // train on route have no train ahead or arrival station is free
        case TrainPerceptionInRoute(p) if !shouldStop(p, train) =>
          val speed = Math.min(p.routeTypology.technology.maxSpeed, train.maxSpeed)
          Running(motionData.withSpeed(speed).updated(dt))
        // train stopped in station starts and run on route
        case TrainPerceptionInStation(p) if p.hasToMove && p.routeTrackIsFree =>
          Running(motionData.withSpeed(train.maxSpeed).updated(dt))
        case _ => this
      }.getOrElse(this)

  /** Running state behavior where train agent is running (with some speed and acceleration)
    * and its current travelled distance is updated.
    *
    * When some train ahead is present, arrival station is not free or have travelled the entire
    * route length it changes to [[Stopped]] otherwise continue running updating travelled distance.
    */
  final case class Running(motionData: MotionData) extends StateBehavior with BasicSpaceManagement:
    override def stateName: String = "Running"

    override def next(agent: Train, dt: Int, p: Percepts): StateBehavior =
      p.map {
        // train on route have a train ahead stops
        case TrainPerceptionInRoute(p) if !enoughSpace(p.trainAheadDistance, agent) => Stopped(motionData)
        // train on route reach destination and can enter in free station then stops
        case TrainPerceptionInRoute(p) if motionData.distanceTravelled >= p.routeLength && p.arrivalStationIsFree =>
          Stopped(MotionDatas.emptyMotionData)
        // train is moving in station can move and track is free update position
        case TrainPerceptionInStation(p) if p.hasToMove && p.routeTrackIsFree => Running(motionData.updated(dt))
        case _                                                                => Running(motionData.updated(dt))
      }.getOrElse(Running(motionData.updated(dt)))
