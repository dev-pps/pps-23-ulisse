package ulisse.entities.simulation.environments.railwayEnvironment

import cats.Id
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.train.TrainAgents.{
  TrainAgent,
  TrainAgentPerception,
  TrainAgentPerceptionData,
  TrainPerceptionInRoute,
  TrainPerceptionInStation,
  TrainRouteInfo,
  TrainStationInfo
}
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.entities.station.StationEnvironment

object PerceptionProvider:

  private def trainPerceptionInStation(train: TrainAgent, env: RailwayEnvironment): TrainStationInfo =
    val trainStationInfo =
      for
        currentDTT        <- env.findCurrentTimeTableFor(train)
        nextDepartureTime <- currentDTT.nextDepartureTime
        departureDelay = Id(nextDepartureTime.asTime) underflowSub Id(env.time)
        nextRoute          <- currentDTT.nextRoute
        (route, direction) <- env.findRouteWithTravelDirection(nextRoute)
      yield TrainStationInfo(departureDelay.toSeconds <= 0, route.isAvailableFor(train, direction))
    trainStationInfo.getOrElse(TrainStationInfo(false, false))

  private def trainPerceptionInRoute(
      train: TrainAgent,
      route: RouteEnvironmentElement,
      env: RailwayEnvironment
  ): TrainRouteInfo =
    def trainAheadDistance: Option[Double] =
      for
        trainTAC     <- route.containers.find(_.contains(train))
        nearestAgent <- trainTAC.trains.find(_.distanceTravelled > train.distanceTravelled)
      yield nearestAgent.distanceTravelled - train.distanceTravelled

    def arrivalStationIsFree: Option[Boolean] =
      for
        currentDTT     <- env.findCurrentTimeTableFor(train)
        currentRoute   <- currentDTT.currentRoute
        arrivalStation <- env.stations.find(_.id == currentRoute._2.id)
      yield arrivalStation.isAvailable
    TrainRouteInfo(route.typology, route.length, trainAheadDistance, arrivalStationIsFree.getOrElse(false))

  /** Provide perception for TrainAgent in RailwayEnvironment */
  given PerceptionProvider[RailwayEnvironment, TrainAgent] with
    type P = TrainAgentPerception[?]
    def perceptionFor(env: RailwayEnvironment, agent: TrainAgent): Option[P] =
      (agent.findIn(env.stations), agent.findIn(env.routes)) match
        case (Some(_), _)     => Some(TrainPerceptionInStation(trainPerceptionInStation(agent, env)))
        case (_, Some(route)) => Some(TrainPerceptionInRoute(trainPerceptionInRoute(agent, route, env)))
        case _                => None
