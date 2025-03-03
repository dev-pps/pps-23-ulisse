package ulisse.entities.simulation.environments

import cats.Id
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Tracks.TrackDirection
import ulisse.entities.route.Tracks.TrackDirection.{Backward, Forward}
import ulisse.entities.simulation.agents.Perceptions.PerceptionProvider
import ulisse.entities.simulation.agents.SimulationAgent
import ulisse.entities.simulation.environments.EnvironmentElements.EnvironmentElement
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.timetable.DynamicTimetables.DynamicTimetable
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentPerception, TrainAgentPerceptionData, TrainRouteInfo, TrainStationInfo}
import ulisse.utils.Times.{ClockTime, Time}
import ulisse.utils.CollectionUtils.updateWhenWithEffects
import ulisse.entities.simulation.environments.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.utils.CollectionUtils.updateWhen

trait RailwayEnvironment extends Environment[RailwayEnvironment]:
  def time: Time
  def stations: Seq[StationEnvironmentElement]
  def routes: Seq[RouteEnvironmentElement]
  override def environmentElements: List[EnvironmentElement] = (stations ++ routes).toList
  def trains: Seq[TrainAgent] =
    (stations.flatMap(_.containers.flatMap(_.trains)) ++ routes.flatMap(_.containers.flatMap(_.trains))).distinct
  override def agents: List[SimulationAgent[?]] = trains.toList
  def timetables: Seq[DynamicTimetable]
  def findCurrentTimeTableFor(train: TrainAgent): Option[DynamicTimetable]
  def findRouteWithTravelDirection(route: (Station, Station)): Option[(RouteEnvironmentElement, TrackDirection)]

object RailwayEnvironment:
  def apply(
             stationsEE: Seq[StationEnvironmentElement],
             routesEE: Seq[RouteEnvironmentElement],
             trains: Seq[TrainAgent],
             dynamicTimetables: Seq[DynamicTimetable]
           ): RailwayEnvironment =
    val distinctStationsEE = stationsEE.distinctBy(_.name)
    val distinctRoutesEE   = routesEE.distinctBy(_.id)
    val distinctTrains     = trains.distinctBy(_.name)
    val distinctDynamicTimetables = dynamicTimetables.distinctBy(table =>
      (table.train.name, table.startStation.name, table.departureTime, table.table)
    )
    val sortedTimetablesByTrainId = orderedTimetablesByTrainId(distinctTrains, distinctDynamicTimetables)
    val stationsEEInitialState    = sortedTimetablesByTrainId.putTrainsInInitialStations(distinctStationsEE)
    RailwayEnvironmentImpl(
      Time(0, 0, 0),
      stationsEEInitialState,
      distinctRoutesEE,
      sortedTimetablesByTrainId.filter(t =>
        stationsEEInitialState.flatMap(_.containers).flatMap(_.trains).contains(t._1)
      ).map(t => (t._1.name, t._2)).toMap
    )

  private def orderedTimetablesByTrainId(
                                          trains: Seq[TrainAgent],
                                          timetables: Seq[DynamicTimetable]
                                        ): List[(TrainAgent, Seq[DynamicTimetable])] =
    def mapTrainsWithTimeTables(
                                 trains: Seq[TrainAgent],
                                 timetables: Seq[DynamicTimetable]
                               ): Seq[(TrainAgent, DynamicTimetable)] =
      timetables.flatMap: tt =>
        trains.find(_.name == tt.train.name) match
          case Some(trainAgent) => Some(trainAgent -> tt)
          case _                => None

    def groupByTrainId(timetables: Seq[(TrainAgent, DynamicTimetable)]): List[(TrainAgent, Seq[DynamicTimetable])] =
      timetables.groupBy(_._1).view.mapValues(_.map(_._2)).toList

    def sortTimetablesByDepartureTime(timetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
    : List[(TrainAgent, Seq[DynamicTimetable])] =
      timetablesByTrainId.map(e => (e._1, e._2.sortBy(_.departureTime)))

    sortTimetablesByDepartureTime(groupByTrainId(mapTrainsWithTimeTables(trains, timetables)).sortBy(_._1.name))

  extension (sortedTimetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
    private def putTrainsInInitialStations(stationsEE: Seq[StationEnvironmentElement])
    : Seq[StationEnvironmentElement] =

      def takeFirstTimetableForTrains(timetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
      : List[(TrainAgent, Option[DynamicTimetable])] =
        timetablesByTrainId.map(e => (e._1, e._2.headOption))

      def updateStationEE(
                           stationsEE: Seq[StationEnvironmentElement],
                           timetable: DynamicTimetable,
                           trainAgent: TrainAgent
                         ): Option[Seq[StationEnvironmentElement]] =
        stationsEE.updateWhenWithEffects(station => station.name == timetable.startStation.name)(
          _.putTrain(trainAgent)
        )

      takeFirstTimetableForTrains(sortedTimetablesByTrainId).foldLeft(stationsEE)((stationsEE, tt) =>
        tt._2.flatMap(updateStationEE(stationsEE, _, tt._1)).getOrElse(stationsEE)
      )

  def empty(): RailwayEnvironment =
    apply(
      Seq[StationEnvironmentElement](),
      Seq[RouteEnvironmentElement](),
      Seq[TrainAgent](),
      Seq[DynamicTimetable]()
    )

  private def trainPerceptionInStation(train: TrainAgent, env: RailwayEnvironment): Option[TrainStationInfo] =
    for
      currentDTT        <- env.findCurrentTimeTableFor(train)
      nextDepartureTime <- currentDTT.nextDepartureTime
      departureDelay = Id(nextDepartureTime) - Id(env.time)
      nextRoute          <- currentDTT.nextRoute
      (route, direction) <- env.findRouteWithTravelDirection(nextRoute)
    yield TrainStationInfo(
      hasToMove = departureDelay.toSeconds <= 0,
      routeTrackIsFree = route.isAvailable(direction)
    )

  given PerceptionProvider[RailwayEnvironment, TrainAgent] with
    type P = TrainAgentPerception
    def perceptionFor(env: RailwayEnvironment, agent: TrainAgent): Option[P] =
      if agent.findIn(env.stations).isDefined then
        agent.findIn(env.stations).map: station =>
          new TrainAgentPerception {
            override def perceptionData: TrainAgentPerceptionData =
              trainPerceptionInStation(agent, env).getOrElse(TrainStationInfo(false, false))
          }
      else
        agent.findIn(env.routes).map: route =>
          new TrainAgentPerception {
            override def perceptionData: TrainAgentPerceptionData =
              TrainRouteInfo(
                routeTypology = route.typology,
                routeLength = route.length,
                trainAheadDistance = route.containers.flatMap(_.trains).find(
                  _.distanceTravelled > agent.distanceTravelled
                ).map(_.distanceTravelled - agent.distanceTravelled),
                arrivalStationIsFree = env.findCurrentTimeTableFor(agent).flatMap(_.currentRoute).flatMap(cr =>
                  env.stations.find(_.name == cr._2.name).map(_.isAvailable)
                ).getOrElse(false)
              )
          }

  private final case class RailwayEnvironmentImpl(
                                                   time: Id[Time],
                                                   stations: Seq[StationEnvironmentElement],
                                                   routes: Seq[RouteEnvironmentElement],
                                                   _timetables: Map[String, Seq[DynamicTimetable]]
                                                 ) extends RailwayEnvironment:
    def timetables: Seq[DynamicTimetable] = _timetables.values.flatten.toSeq
    def doStep(dt: Int): RailwayEnvironment =
      // Allow agents to be at the same time in more than an environment element
      // that because an agent when enters a station doesn't leave immediately the route and vice versa
      // also for future improvements, an agent when crossing two rails will be in two rails at the same time
      // NOTE: For now agent will be in only one station or route
      trains.map(a => a.doStep(dt, this)).foldLeft(this) { (env, updatedTrain) =>
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
      if agent.findIn(routes).isDefined then updateAgentOnRoute(agent, time)
      else updateAgentInStation(agent, time)

    private def updateAgentOnRoute(agent: TrainAgent, time: Time): Option[RailwayEnvironmentImpl] =
      agent.findIn(routes).flatMap(r =>
        println("ROUTE UPDATE FUNCTION")
        routeUpdateFunction(r, agent, time)
      )

    private def updateAgentInStation(agent: TrainAgent, time: Time): Option[RailwayEnvironmentImpl] =
      agent.findIn(stations).flatMap(s =>
        println("STATION UPDATE FUNCTION")
        stationUpdateFunction(s, agent, time)
      )

    private def routeUpdateFunction(
                                     route: RouteEnvironmentElement,
                                     agent: TrainAgent,
                                     time: Time
                                   ): Option[RailwayEnvironmentImpl] =
      agent.distanceTravelled match
        case d if d >= route.length =>
          for
            ree <- route.removeTrain(agent)
            re = routes.updateWhen(_.id == ree.id)(_ => ree)
            _  = println(s"REMOVE TRAIN FROM ROUTE")
            tt  <- findCurrentTimeTableFor(agent)
            ct  <- ClockTime(time.h, time.m).toOption
            utt <- tt.arrivalUpdate(ct)
            _  = println(s"AU: ${utt.effectiveTable}]")
            dt = _timetables.map((k, v) => if v.contains(tt) then (k, v.updateWhen(_ == tt)(_ => utt)) else (k, v))
            currentRoute <- tt.currentRoute
            see          <- stations.find(currentRoute._2.name == _.name)
            usee         <- see.putTrain(agent.resetDistanceTravelled())
            se = stations.updateWhen(_.name == usee.name)(_ => usee)
          yield copy(stations = se, routes = re, _timetables = dt)
        case _ => route.updateTrain(agent).map(ree => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree)))

    private def stationUpdateFunction(
                                       station: StationEnvironmentElement,
                                       agent: TrainAgent,
                                       time: Time
                                     ): Option[RailwayEnvironmentImpl] =
      for
        see <- station.removeTrain(agent)
        se = stations.updateWhen(_.name == see.name)(_ => see)
        tt  <- findCurrentTimeTableFor(agent)
        utt <- tt.departureUpdate(ClockTime(time.h, time.m).getOrDefault)
        dt = _timetables.map((k, v) => if v.contains(tt) then (k, v.updateWhen(_ == tt)(_ => utt)) else (k, v))
        nextRoute         <- tt.nextRoute
        routeAndDirection <- findRouteWithTravelDirection(nextRoute)
        ree               <- routes.find(routeAndDirection._1.id == _.id)
        uree              <- ree.putTrain(agent.resetDistanceTravelled(), routeAndDirection._2)
        re = routes.updateWhen(_.id == uree.id)(_ => uree)
      yield copy(stations = se, routes = re, _timetables = dt)

    def findCurrentTimeTableFor(train: TrainAgent): Option[DynamicTimetable] =
      _timetables.get(train.name).flatMap(_.find(!_.completed))

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

