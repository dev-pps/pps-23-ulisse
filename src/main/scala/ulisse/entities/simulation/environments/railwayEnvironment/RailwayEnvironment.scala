package ulisse.entities.simulation.environments.railwayEnvironment

import cats.Id
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.route.Tracks.TrackDirection.{Backward, Forward}
import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.EnvironmentElement
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.entities.simulation.environments.Environment
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.station.Station
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.*
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.{updateWhen, updateWhenWithEffects}
import ulisse.utils.Times.{ClockTime, Time}

/** Simulation Environment for Railway simulations */
trait RailwayEnvironment extends Environment[RailwayEnvironment]:
  /** simulation time */
  def time: Time

  /** stations in the environment */
  def stations: Seq[StationEnvironmentElement]

  /** routes in the environment */
  def routes: Seq[RouteEnvironmentElement]

  /** ordered timetables by departure time grouped by train */
  def timetablesByTrain: Map[Train, Seq[DynamicTimetable]]

  /** timetables in the environment */
  def timetables: Seq[DynamicTimetable] = timetablesByTrain.values.flatten.toSeq

  /** environment elements in the environment */
  override def environmentElements: List[EnvironmentElement] = (stations ++ routes ++ timetables).toList

  /** trains in the environment */
  def trains: Seq[TrainAgent] =
    (stations.flatMap(_.containers.flatMap(_.trains)) ++ routes.flatMap(_.containers.flatMap(_.trains))).distinct

  /** agents in the environment */
  override def agents: List[SimulationAgent[?]] = trains.toList

  /** find active timetable for an agent */
  def findCurrentTimeTableFor(train: TrainAgent): Option[DynamicTimetable]

  /** find route with travel direction */
  def findRouteWithTravelDirection(route: (Station, Station)): Option[(RouteEnvironmentElement, TrackDirection)]

/** Factory for [[RailwayEnvironment]] instances */
object RailwayEnvironment:
  def apply(
      startTime: Time,
      configurationData: ConfigurationData
  ): RailwayEnvironment =
    RailwayEnvironmentImpl(
      startTime,
      configurationData.stations,
      configurationData.routes,
      configurationData.timetables
    )

  def auto(
      configurationData: ConfigurationData
  ): RailwayEnvironment =
    apply(
      Time.secondsToTime(
        configurationData.timetables.values.flatten.map(_.departureTime.toSeconds).foldLeft(0)(math.min)
      ),
      configurationData
    )
  def default(configurationData: ConfigurationData): RailwayEnvironment =
    apply(
      Time(0, 0, 0),
      configurationData
    )

  def empty(): RailwayEnvironment =
    default(ConfigurationData.empty())

  private final case class RailwayEnvironmentImpl(
      time: Id[Time],
      stations: Seq[StationEnvironmentElement],
      routes: Seq[RouteEnvironmentElement],
      timetablesByTrain: Map[Train, Seq[DynamicTimetable]]
  ) extends RailwayEnvironment:
    def doStep(dt: Int): RailwayEnvironment =
      // Allow agents to be at the same time in more than an environment element
      // that because an agent when enters a station doesn't leave immediately the route and vice versa
      // also for future improvements, an agent when crossing two rails will be in two rails at the same time
      // NOTE: For now agent will be in only one station or route
      trains.map(_.doStep(dt, this)).foldLeft(this) { (env, updatedTrain) =>
        updatedTrain match
          case Some(updatedTrain) =>
            // Update Idea: startByMovingAgent
            // If is already on a route,
            //  could remain completely on the route
            //  or enter the station and so leave the route
            // If is already on a station,
            //  could enter the route and so leave the station
            env.updateEnvironmentWith(updatedTrain, time + Time(0, 0, dt)).getOrElse(env)
          case _ => this
      }

    private def updateEnvironmentWith(agent: TrainAgent, time: Time): Option[RailwayEnvironmentImpl] =
      (agent.findIn(routes), agent.findIn(stations)) match
        case (Some(route), _)   => routeUpdateFunction(route, agent, time)
        case (_, Some(station)) => stationUpdateFunction(station, agent, time)
        case _                  => None

    private def routeUpdateFunction(
        route: RouteEnvironmentElement,
        agent: TrainAgent,
        time: Time
    ): Option[RailwayEnvironmentImpl] =
      agent.distanceTravelled match
        case d if d >= route.length =>
          for
            updatedRoute <- route.removeTrain(agent)
            updatedRoutes = routes.updateWhen(_.id == updatedRoute.id)(_ => updatedRoute)
            (updatedTimetables, currentRoute) <-
              timetableUpdateFunction(_.arrivalUpdate(_), _.currentRoute, agent, time)
            station        <- stations.find(currentRoute._2.name == _.name)
            updatedStation <- station.putTrain(agent.resetDistanceTravelled())
            updatedStations = stations.updateWhen(_.name == updatedStation.name)(_ => updatedStation)
          yield copy(stations = updatedStations, routes = updatedRoutes, timetablesByTrain = updatedTimetables)
        case _ => route.updateTrain(agent).map(ree => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree)))

    private def stationUpdateFunction(
        station: StationEnvironmentElement,
        agent: TrainAgent,
        time: Time
    ): Option[RailwayEnvironmentImpl] =
      for
        updatedStation <- station.removeTrain(agent)
        updatedStations = stations.updateWhen(_.name == updatedStation.name)(_ => updatedStation)
        (updatedTimetables, nextRoute) <- timetableUpdateFunction(_.departureUpdate(_), _.nextRoute, agent, time)
        routeAndDirection              <- findRouteWithTravelDirection(nextRoute)
        route                          <- routes.find(routeAndDirection._1.id == _.id)
        updatedRoute                   <- route.putTrain(agent.resetDistanceTravelled(), routeAndDirection._2)
        updatedRoutes = routes.updateWhen(_.id == updatedRoute.id)(_ => updatedRoute)
      yield copy(stations = updatedStations, routes = updatedRoutes, timetablesByTrain = updatedTimetables)

    private def timetableUpdateFunction(
        updateF: (DynamicTimetable, ClockTime) => Option[DynamicTimetable],
        routeInfo: DynamicTimetable => Option[(Station, Station)],
        agent: TrainAgent,
        time: Time
    ): Option[(Map[Train, Seq[DynamicTimetable]], (Station, Station))] =
      for
        currentTimetable <- findCurrentTimeTableFor(agent)
        currentClockTime <- ClockTime(time.h, time.m).toOption
        updatedTimetable <- updateF(currentTimetable, currentClockTime)
        updatedTimetables = timetablesByTrain.view.mapValues(
          _.updateWhen(_ == currentTimetable)(_ => updatedTimetable)
        ).toMap
        info <- routeInfo(currentTimetable)
      yield (updatedTimetables, info)

    def findCurrentTimeTableFor(train: TrainAgent): Option[DynamicTimetable] =
      timetablesByTrain(train).find(!_.completed)

    def findRouteWithTravelDirection(route: (Station, Station)): Option[(RouteEnvironmentElement, TrackDirection)] =
      extension (r: Route)
        private def matchStations(departure: Station, arrival: Station): Boolean =
          r.departure.name == departure.name && r.arrival.name == arrival.name

      def findRoute(departure: Station, arrival: Station): Option[RouteEnvironmentElement] =
        routes.find(_.matchStations(departure, arrival))

      (findRoute(route._1, route._2), findRoute(route._2, route._1)) match
        case (Some(r), _) => Some((r, Forward))
        case (_, Some(r)) => Some((r, Backward))
        case _            => None
