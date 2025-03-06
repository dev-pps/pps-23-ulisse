package ulisse.entities.simulation.environments.railwayEnvironment

import cats.Id
import ulisse.entities.route.{RouteEnvironment, RouteEnvironmentElement}
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.route.Tracks.TrackDirection.{Backward, Forward}
import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.EnvironmentElement
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.entities.simulation.environments.Environments.EnvironmentsCoordinator
import ulisse.entities.station.{Station, StationEnvironment, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetableEnvironment
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.TrainAgent
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.{updateWhen, updateWhenWithEffects}
import ulisse.utils.Times.{ClockTime, Time}
import ulisse.entities.timetable.DynamicTimetables.*

/** Simulation Environment for Railway simulations */
trait RailwayEnvironment extends EnvironmentsCoordinator[RailwayEnvironment]:
  /** simulation time */
  def time: Time

  /** stations in the environment */
  def stations: Seq[StationEnvironmentElement]

  /** routes in the environment */
  def routes: Seq[RouteEnvironmentElement]

  /** Dynamic timetable environment */
  def dynamicTimetableEnvironment: DynamicTimetableEnvironment

  def timetables: Seq[DynamicTimetable] = dynamicTimetableEnvironment.environmentElements

  /** environment elements in the environment */
  def environmentElements: List[EnvironmentElement] = (stations ++ routes ++ timetables).toList

  /** trains in the environment */
  def trains: Seq[TrainAgent] =
    (stations.flatMap(_.containers.flatMap(_.trains)) ++ routes.flatMap(_.containers.flatMap(_.trains))).distinct

  /** agents in the environment */
  def agents: List[SimulationAgent[?]] = trains.toList

//  /** find active timetable for an agent */
//  def findCurrentTimeTableFor(train: TrainAgent): Option[DynamicTimetable]

  def routeEnvironment: RouteEnvironment
  def stationEnvironment: StationEnvironment

/** Factory for [[RailwayEnvironment]] instances */
object RailwayEnvironment:
  def apply(
      startTime: Time,
      configurationData: ConfigurationData
  ): RailwayEnvironment =
    RailwayEnvironmentImpl(
      startTime,
      StationEnvironment(configurationData),
      RouteEnvironment(configurationData),
      DynamicTimetableEnvironment(configurationData)
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
      stationEnvironment: StationEnvironment,
      routeEnvironment: RouteEnvironment,
      dynamicTimetableEnvironment: DynamicTimetableEnvironment
  ) extends RailwayEnvironment:
    export routeEnvironment.environmentElements as routes, stationEnvironment.environmentElements as stations,
      dynamicTimetableEnvironment.dynamicTimetablesByTrain as timetablesByTrain
    override def environments = Seq(routeEnvironment)
    def doStep(dt: Int): RailwayEnvironment =
      // Allow agents to be at the same time in more than an environment element
      // that because an agent when enters a station doesn't leave immediately the route and vice versa
      // also for future improvements, an agent when crossing two rails will be in two rails at the same time
      // NOTE: For now agent will be in only one station or route

      trains.map(_.doStep(dt, this)).foldLeft(this) { (env, updatedTrain) =>
        // Update Idea: startByMovingAgent
        // If is already on a route,
        //  could remain completely on the route
        //  or enter the station and so leave the route
        // If is already on a station,
        //  could enter the route and so leave the station
        env.updateEnvironmentWith(updatedTrain, time + Time(0, 0, dt)).getOrElse(env)
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
      agent.motionData.distanceTravelled match
        case d if d >= route.length =>
          for
            updatedRoutes <- routeEnvironment.removeTrain(agent)
            (updatedTimetables, currentRoute) <-
              dynamicTimetableEnvironment.updateTables(_.arrivalUpdate(_), _.currentRoute, agent, time)
            updatedStations <- stationEnvironment.putTrain(agent, currentRoute._2)
          yield copy(
            stationEnvironment = updatedStations,
            routeEnvironment = updatedRoutes,
            dynamicTimetableEnvironment = updatedTimetables
          )
        case _ => routeEnvironment.updateTrain(agent).map(updatedRoutes => copy(routeEnvironment = updatedRoutes))

    private def stationUpdateFunction(
        station: StationEnvironmentElement,
        agent: TrainAgent,
        time: Time
    ): Option[RailwayEnvironmentImpl] =
      for
        updatedStations <- stationEnvironment.removeTrain(agent)
        (updatedTimetables, nextRoute) <-
          dynamicTimetableEnvironment.updateTables(_.departureUpdate(_), _.nextRoute, agent, time)
        updatedRoutes <- routeEnvironment.putTrain(agent, nextRoute)
      yield copy(
        stationEnvironment = updatedStations,
        routeEnvironment = updatedRoutes,
        dynamicTimetableEnvironment = updatedTimetables
      )

//    def findCurrentTimeTableFor(train: TrainAgent): Option[DynamicTimetable] =
//      timetablesByTrain(train).find(!_.completed)
