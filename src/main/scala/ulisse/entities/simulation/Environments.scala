package ulisse.entities.simulation

import cats.Id
import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.Track.TrainAgentsDirection
import ulisse.entities.route.Track.TrainAgentsDirection.{Backward, Forward}
import ulisse.entities.simulation.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.entities.simulation.Perceptions.PerceptionProvider
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.timetable.{DynamicTimetable, Timetables, TrainStationTime}
import ulisse.entities.timetable.Timetables.Timetable
import ulisse.entities.train.TrainAgents
import ulisse.entities.train.TrainAgents.{
  TrainAgent,
  TrainAgentPerception,
  TrainAgentPerceptionData,
  TrainRouteInfo,
  TrainStationInfo
}
import ulisse.entities.train.Trains.Train
import ulisse.utils.CollectionUtils.*
import ulisse.utils.Times
import ulisse.entities.timetable.DynamicTimetable.*
import ulisse.utils.Times.{ClockTime, Time}

import scala.collection.immutable.ListMap

object Environments:
  trait Environment

  trait RailwayEnvironment extends Environment:
    def time: Time
    def doStep(dt: Int): RailwayEnvironment
    def stations: Seq[StationEnvironmentElement]
    def routes: Seq[RouteEnvironmentElement]
    def agents: Seq[SimulationAgent] =
      (stations.flatMap(_.containers.flatMap(_.trains)) ++ routes.flatMap(_.containers.flatMap(_.trains))).distinct
    def timetables: Seq[DynamicTimetable]
    def perceptionFor[A <: SimulationAgent](agent: A)(using
        provider: PerceptionProvider[RailwayEnvironment, A]
    ): Option[provider.P] =
      provider.perceptionFor(this, agent)

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
          println(s"eya $tt")
          tt._2.flatMap(updateStationEE(stationsEE, _, tt._1)).getOrElse(stationsEE)
        )

    def empty(): RailwayEnvironment =
      apply(
        Seq[StationEnvironmentElement](),
        Seq[RouteEnvironmentElement](),
        Seq[TrainAgent](),
        Seq[DynamicTimetable]()
      )

    given PerceptionProvider[RailwayEnvironment, TrainAgent] with
      type P = TrainAgentPerception
      def perceptionFor(railwayEnvironment: RailwayEnvironment, agent: TrainAgent): Option[P] =
        agent.findIn(railwayEnvironment.stations) match
          case Some(station) => Some(new TrainAgentPerception {
              override def perceptionData: TrainAgentPerceptionData = TrainStationInfo(true, true)
            })
          case _ => agent.findIn(railwayEnvironment.routes) match
              case Some(route) => Some(new TrainAgentPerception {
                  override def perceptionData: TrainAgentPerceptionData =
                    TrainRouteInfo(route.typology, route.length, None, true)
                })
              case _ => None

    private final case class RailwayEnvironmentImpl(
        time: Id[Time],
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        _timetables: Map[String, Seq[DynamicTimetable]]
    ) extends RailwayEnvironment:
      println(_timetables)
      def timetables: Seq[DynamicTimetable] = _timetables.values.flatten.toSeq
      def doStep(dt: Int): RailwayEnvironment =
        // Allow agents to be at the same time in more than an environment element
        // that because an agent when enters a station doesn't leave immediately the route and vice versa
        // also for future improvements, an agent when crossing two rails will be in two rails at the same time
        // NOTE: For now agent will be in only one station or route
        val agentsWithActions = agents.map(a => a -> a.doStep(dt, this))
        println(agentsWithActions)
        val env = agentsWithActions.foldLeft(this) { (env, agentWithAction) =>
          agentWithAction match
            case (agent: TrainAgent, Some(Actions.MoveBy(d))) =>
              // Update Idea: startByMovingAgent
              // If is already on a route,
              //  could remain completely on the route
              //  or enter the station and so leave the route
              // If is already on a station,
              //  could enter the route and so leave the station
              println(s"MOVE d: $d")
              import ulisse.utils.Times.given

              env.updateEnvironmentWith(agent.updateDistanceTravelled(d), time + Time(0, 0, dt))
            case _ =>
              println("NO ACTION")
              this
        }
        println(s"ENV$env")
        env

      private def updateEnvironmentWith(agent: TrainAgent, time: Time): RailwayEnvironmentImpl =
        println("updateEnvironmentWith")
        updateAgentOnRoute(agent, time).getOrElse(updateAgentInStation(agent, time).getOrElse(this))
      private def updateAgentOnRoute(agent: TrainAgent, time: Time): Option[RailwayEnvironmentImpl] =
        println("updateAgentOnRoute")
        agent.findIn(routes).map(ree =>
          println("updateAgentOnRoute")
          routeUpdateFunction(ree, agent, time)
        )
      private def updateAgentInStation(agent: TrainAgent, time: Time): Option[RailwayEnvironmentImpl] =
        agent.findIn(stations).map(see =>
          println("updateAgentInStation")
          stationUpdateFunction(see, agent, time) match
            case Some(re) =>
              println("ok")
              re
            case _ =>
              println("FFFFF")
              this
        )

      private def routeUpdateFunction(
          route: RouteEnvironmentElement,
          agent: TrainAgent,
          time: Time
      ): RailwayEnvironmentImpl =
        agent.distanceTravelled match
          case d if d >= route.length =>
            (for
              ree <- route.removeTrain(agent)
              re = routes.updateWhen(_.id == ree.id)(_ => ree)
              tt  <- findCurrentTimeTable(agent)
              utt <- tt.arrivalUpdate(ClockTime(time.h, time.m).getOrDefault)
              dt = _timetables.toList.updateWhen(_._1 == agent.name)(e =>
                (e._1, e._2.updateWhen(dtt => dtt.table == utt.table)(_ => utt))
              ).toMap
              currentRoute <- tt.currentRoute
              see          <- stations.find(currentRoute._2.name == _.name)
              usee         <- see.putTrain(agent.resetDistanceTravelled())
              se = stations.updateWhen(_.name == usee.name)(_ => usee)
            yield copy(stations = se, routes = re, _timetables = dt)).getOrElse(this)
          case _ => route.updateTrain(agent) match
              case Some(ree) => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree))
              case _         => this

      private def stationUpdateFunction(
          station: StationEnvironmentElement,
          agent: TrainAgent,
          time: Time
      ): Option[RailwayEnvironmentImpl] =
        for
          see <- station.removeTrain(agent)
          se = stations.updateWhen(_.name == see.name)(_ => see)
          tt  <- findCurrentTimeTable(agent)
          utt <- tt.departureUpdate(ClockTime(time.h, time.m).getOrDefault)
          dt = _timetables.toList.updateWhen(_._1 == agent.name)(e =>
            (e._1, e._2.updateWhen(dtt => dtt.table == utt.table)(_ => utt))
          ).toMap
          nextRoute         <- tt.nextRoute
          routeAndDirection <- findRouteDirection(nextRoute)
          ree               <- routes.find(routeAndDirection._1.id == _.id)
          uree              <- ree.putTrain(agent.resetDistanceTravelled(), routeAndDirection._2)
          re = routes.updateWhen(_.id == uree.id)(_ => uree)
        yield copy(stations = se, routes = re, _timetables = dt)

      private def findCurrentTimeTable(train: TrainAgent): Option[DynamicTimetable] =
        val tt = _timetables.get(train.name)
        println(tt)
        tt.flatMap(_.find(!_.completed))

      private def findRouteDirection(route: (Station, Station)): Option[(Route, TrainAgentsDirection)] =
        extension (r: Route)
          private def matchStations(departure: Station, arrival: Station): Boolean =
            r.departure.name == departure.name && r.arrival.name == arrival.name

        def findRoute(departure: Station, arrival: Station): Option[Route] =
          println(routes)
          routes.find(_.matchStations(departure, arrival))
        val p = (findRoute(route._1, route._2), findRoute(route._2, route._1))
        println(p)
        p match
          case (Some(r), _) => Some((r, Forward))
          case (_, Some(r)) => Some((r, Backward))
          case _            => None
