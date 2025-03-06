package ulisse.entities.simulation.environments.railwayEnvironment

import cats.Id
import ulisse.entities.route.{RouteEnvironment, RouteEnvironmentElement}
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
  /** Simulation time */
  def time: Time

  /** Route environment of the simulation */
  def routeEnvironment: RouteEnvironment

  /** Station environment of the simulation */
  def stationEnvironment: StationEnvironment

  /** Dynamic timetable environment of the simulation */
  def dynamicTimetableEnvironment: DynamicTimetableEnvironment

  /** The environment of the simulation */
  override def environments: Seq[Environment[?]] =
    Seq(routeEnvironment, stationEnvironment, dynamicTimetableEnvironment)

  /** Routes in the environment */
  def routes: Seq[RouteEnvironmentElement] = routeEnvironment.environmentElements

  /** stations in the environment */
  def stations: Seq[StationEnvironmentElement] = stationEnvironment.environmentElements

  /** timetables in the environment */
  def timetables: Seq[DynamicTimetable] = dynamicTimetableEnvironment.environmentElements

  /** timetables by train in the environment */
  def timetablesByTrain: Map[Train, Seq[DynamicTimetable]] = dynamicTimetableEnvironment.dynamicTimetablesByTrain

  /** environment elements in the environment */
  def environmentElements: List[EnvironmentElement] = (stations ++ routes ++ timetables).toList

  /** trains in the environment */
  def trains: Seq[TrainAgent] =
    (stations.flatMap(_.containers.flatMap(_.trains)) ++ routes.flatMap(_.containers.flatMap(_.trains))).distinct

  /** agents in the environment */
  override def agents: List[SimulationAgent[?]] = trains.toList

/** Factory for [[RailwayEnvironment]] instances */
object RailwayEnvironment:

  /** Create a new RailwayEnvironment */
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

  /** Create a new RailwayEnvironment with the start time set to the earliest departure time */
  def auto(
      configurationData: ConfigurationData
  ): RailwayEnvironment =
    apply(
      Time.secondsToTime(
        configurationData.timetablesByTrain.values.flatten.map(_.departureTime.toSeconds).foldLeft(0)(math.min)
      ),
      configurationData
    )

  /** Create a new RailwayEnvironment with the start time set to 00:00 */
  def default(configurationData: ConfigurationData): RailwayEnvironment =
    apply(
      Time(0, 0, 0),
      configurationData
    )

  /** Create a new empty RailwayEnvironment */
  def empty(): RailwayEnvironment =
    default(ConfigurationData.empty())

  private final case class RailwayEnvironmentImpl(
      time: Id[Time],
      stationEnvironment: StationEnvironment,
      routeEnvironment: RouteEnvironment,
      dynamicTimetableEnvironment: DynamicTimetableEnvironment
  ) extends RailwayEnvironment:
    override def doStep(dt: Int): RailwayEnvironment =
      trains.map(_.doStep(dt, this)).foldLeft(this): (env, updatedTrain) =>
        env.updateEnvironmentWith(updatedTrain, time + Time(0, 0, dt)).getOrElse(env)

    private def updateEnvironmentWith(agent: TrainAgent, time: Time): Option[RailwayEnvironmentImpl] =
      (agent.findIn(routes), agent.findIn(stations)) match
        case (Some(route), _)   => routeUpdateFunction(route, agent, time)
        case (_, Some(station)) => stationUpdateFunction(station, agent, time)
        case _                  => None

    private def swapFromRouteToStation(agent: TrainAgent): Option[RailwayEnvironmentImpl] =
      for
        updatedRoutes <- routeEnvironment.removeTrain(agent)
        (updatedTimetables, currentRoute) <-
          dynamicTimetableEnvironment.updateTables(_.arrivalUpdate(_), _.currentRoute, agent, time)
        updatedStations <- stationEnvironment.putTrain(agent, currentRoute._2)
      yield copy(time, updatedStations, updatedRoutes, updatedTimetables)

    private def routeUpdateFunction(
        route: RouteEnvironmentElement,
        agent: TrainAgent,
        time: Time
    ): Option[RailwayEnvironmentImpl] =
      agent.distanceTravelled match
        case d if d >= route.length => swapFromRouteToStation(agent)
        case _ => routeEnvironment.updateTrain(agent).map(updatedRoutes => copy(time, routeEnvironment = updatedRoutes))

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
      yield copy(time, updatedStations, updatedRoutes, updatedTimetables)
