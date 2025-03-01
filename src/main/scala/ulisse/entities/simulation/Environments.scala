package ulisse.entities.simulation

import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
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

import scala.collection.immutable.ListMap

object Environments:
  trait Environment

  trait RailwayEnvironment extends Environment:
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
      val timetablesByTrainId    = orderedTimetablesByTrainId(trains, dynamicTimetables)
      val stationsEEInitialState = timetablesByTrainId.putTrainsInInitialStations(stationsEE)
      RailwayEnvironmentImpl(
        stationsEEInitialState,
        routesEE,
        timetablesByTrainId.filter(t =>
          stationsEEInitialState.flatMap(_.containers).flatMap(_.trains).contains(t._1)
        ).toMap
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

      def sortTimetablesByTrainName(timetables: Seq[(TrainAgent, DynamicTimetable)])
          : Seq[(TrainAgent, DynamicTimetable)] =
        timetables.sortBy(_._1.name)

      groupByTrainId(mapTrainsWithTimeTables(trains, timetables)).sortBy(_._1.name)

//        .groupBy(_._1).view.mapValues(_.map(_._2)).map(t =>
//        (t._1, t._2.sortBy(_.departureTime))
//      ).toList.sortBy(_._1.name)

    extension (timetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
      private def putTrainsInInitialStations(stationsEE: Seq[StationEnvironmentElement])
          : Seq[StationEnvironmentElement] =
        def sortTimetablesByDepartureTime(timetablesByTrainId: List[(TrainAgent, Seq[DynamicTimetable])])
            : List[(TrainAgent, Seq[DynamicTimetable])] =
          timetablesByTrainId.map(e => (e._1, e._2.sortBy(_.departureTime)))

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

        takeFirstTimetableForTrains(sortTimetablesByDepartureTime(timetablesByTrainId)).foldLeft(stationsEE)(
          (stationsEE, tt) =>
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
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        _timetables: Map[Train, Seq[DynamicTimetable]]
    ) extends RailwayEnvironment:

      def timetables: Seq[DynamicTimetable] = _timetables.values.flatten.toSeq
      def doStep(dt: Int): RailwayEnvironment =
        // Allow agents to be at the same time in more than an environment element
        // that because an agent when enters a station doesn't leave immediately the route and vice versa
        // also for future improvements, an agent when crossing two rails will be in two rails at the same time
        // NOTE: For now agent will be in only one station or route
        val agentsWithActions = agents.map(a => a -> a.doStep(dt, this))
        agentsWithActions.foldLeft(this) { (env, agentWithAction) =>
          agentWithAction match
            case (agent: TrainAgent, Some(Actions.MoveBy(d))) =>
              // Update Idea: startByMovingAgent
              // If is already on a route,
              //  could remain completely on the route
              //  or enter the station and so leave the route
              // If is already on a station,
              //  could enter the route and so leave the station
              env.updateEnvironmentWith(agent.updateDistanceTravelled(d))
            case _ => this
        }

      private def updateEnvironmentWith(agent: TrainAgent): RailwayEnvironmentImpl =
        updateAgentOnRoute(agent).updateAgentInStation(agent)
      private def updateAgentOnRoute(agent: TrainAgent): RailwayEnvironmentImpl =
        agent.findIn(routes).fold(this)(ree => routeUpdateFunction(ree, agent))
      private def updateAgentInStation(agent: TrainAgent): RailwayEnvironmentImpl =
        agent.findIn(stations).fold(this)(see => copy(stations = stations.updateWhen(_ == see)(s => s)))

      private def routeUpdateFunction(route: RouteEnvironmentElement, agent: TrainAgent): RailwayEnvironmentImpl =
        agent.distanceTravelled match
          case d if d >= route.length + agent.length =>
            route.removeTrain(agent) match
              case Some(ree) => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree))
              case _         => this
          case d if d >= route.length =>
            (
              stations.find(_.name == route.arrival.name).flatMap(_.putTrain(agent)),
              route.updateTrain(agent)
            ) match
              case (Some(see), Some(ree)) =>
                copy(stations.updateWhen(_.name == see.name)(_ => see), routes.updateWhen(_.id == ree.id)(_ => ree))
              case _ => this
          case _ => route.updateTrain(agent) match
              case Some(ree) => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree))
              case _         => this

      private def stationUpdateFunction(
          station: StationEnvironmentElement,
          agent: TrainAgent
      ): RailwayEnvironmentImpl =
        agent.distanceTravelled match
          case d if d >= agent.length =>
            station.removeTrain(agent) match
              case Some(see) => copy(stations = stations.updateWhen(_.name == see.name)(_ => see))
              case _         => this
          case _ => station.updateTrain(agent) match
              case Some(see) => copy(stations = stations.updateWhen(_.name == see.name)(_ => see))
              case _         => this
