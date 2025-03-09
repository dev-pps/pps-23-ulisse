package ulisse.entities.train

import ulisse.entities.route.Routes.RouteType
import ulisse.entities.simulation.agents.Perceptions.{Perception, PerceptionData}

/** Object containing all specific train agent [[PerceptionData]] perceptions that can receive and to which reacts.
  *
  * Contains data structure of each type of [[TrainAgentPerceptionData]] that train agent can receive, these [[PerceptionData]]
  * are then wrapped inside specific type of [[TrainAgentPerception]] depending on source of perceptions (from station or route).
  */
object TrainAgentPerceptions:

  /** Main type of TrainAgent perception data. Represents data used to take next move decision. */
  sealed trait TrainAgentPerceptionData extends PerceptionData

  /** Station perception data, a specific type of [[TrainAgentPerceptionData]]
    *
    * Two information are provided: `hasToMove` and `routeTrackIsFree`
    */
  sealed trait TrainStationInfo extends TrainAgentPerceptionData:
    /** Returns true if train should start from station (based on timetable) */
    def hasToMove: Boolean

    /** Returns true if route out of station is free and train can move out of station. */
    def routeTrackIsFree: Boolean

  /** Companion object of trait [[TrainStationInfo]] */
  object TrainStationInfo:
    def apply(hasToMove: Boolean, routeTrackIsFree: Boolean): TrainStationInfo =
      TrainStationInfoImpl(hasToMove, routeTrackIsFree)
    private final case class TrainStationInfoImpl(hasToMove: Boolean, routeTrackIsFree: Boolean)
        extends TrainStationInfo

  /** Route perception data, a specific type of [[TrainAgentPerceptionData]]
    * All info useful to train to understand how to move on route during trip.
    */
  sealed trait TrainRouteInfo extends TrainAgentPerceptionData:
    /** Returns [[RouteType]] of route on which train is running. Is used to regulate train max speed. */
    def routeTypology: RouteType

    /** Returns length of route to be travelled */
    def routeLength: Double

    /** Returns some distance if there is some another train ahead otherwise None. */
    def trainAheadDistance: Option[Double]

    /** Returns true if arrival station has at least one platform track free. */
    def arrivalStationIsFree: Boolean

  /** Companion object of [[TrainRouteInfo]] */
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

  /** Represent train agent perception type of [[Perception]] that is sent by environment
    *
    * It wraps [[TrainAgentPerceptionData]] and TODO finire
    */
  trait TrainAgentPerception[PD <: PerceptionData] extends Perception[PD]

  case class TrainPerceptionInStation(perceptionData: TrainStationInfo) extends TrainAgentPerception[TrainStationInfo]

  case class TrainPerceptionInRoute(perceptionData: TrainRouteInfo) extends TrainAgentPerception[TrainRouteInfo]
