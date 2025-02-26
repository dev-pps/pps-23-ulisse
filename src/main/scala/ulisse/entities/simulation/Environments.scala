package ulisse.entities.simulation

import ulisse.entities.Coordinate
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.EnvironmentElements.TrainAgentEEWrapper.findIn
import ulisse.entities.simulation.EnvironmentElements.TrainAgentsDirection.Forward
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.timetable.{Timetables, TrainStationTime}
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

import scala.collection.immutable.ListMap

object Environments:
  trait Environment
  trait Perception[PD <: PerceptionData]:
    def perceptionData: PD
  trait PerceptionData

  trait PerceptionProvider[E <: Environment, A <: SimulationAgent]:
    type P <: Perception[?]
    def perceptionFor(environment: E, agent: A): Option[P]
  trait RailwayEnvironment extends Environment:
    def doStep(dt: Int): RailwayEnvironment
    def stations: Seq[StationEnvironmentElement]
    def routes: Seq[RouteEnvironmentElement]
    def agents: Seq[SimulationAgent] =
      (stations.flatMap(_.containers.flatMap(_.trains)) ++ routes.flatMap(_.containers.flatMap(_.trains))).distinct
    def perceptionFor[A <: SimulationAgent](agent: A)(using
        provider: PerceptionProvider[RailwayEnvironment, A]
    ): Option[provider.P] =
      provider.perceptionFor(this, agent)

  object RailwayEnvironment:
    // TODO evaluate where to do the initial placement of the trains
    def apply(
        stations: Seq[Station],
        routes: Seq[Route],
        agents: Seq[Train],
        timetables: Seq[Timetable] // sequenza disordinata di timetables relative ai vari treni
    ): RailwayEnvironment =
      val stationsEE = stations.map(StationEnvironmentElement(_))
      val routesEE   = routes.map(RouteEnvironmentElement(_, 0.0))
//      val dynamicTimeTables = timetables.map(DynamicTimeTable(_))
      val schedulesMap          = orderedScheduleByTrain(timetables)
      val stationEEInitialState = schedulesMap.putTrainsInInitialStations(stationsEE)
      SimulationEnvironmentImpl(stationEEInitialState, routesEE)

    def orderedScheduleByTrain(timetables: Seq[Timetable]): Map[TrainAgent, Seq[List[(Station, TrainStationTime)]]] =
      timetables.map(tt => TrainAgent(tt.train) -> tt.table).groupBy(_._1).view.mapValues(_.map(_._2)).toMap.map(t =>
        (t._1, t._2.orderSchedule())
      )
    extension (schedulesMap: Map[TrainAgent, Seq[List[(Station, TrainStationTime)]]])
      def putTrainsInInitialStations(stationsEE: Seq[StationEnvironmentElement]): Seq[StationEnvironmentElement] =
        schedulesMap.foldLeft(stationsEE)((stationsEE, e) =>
          stationsEE.updateWhenWithEffects(_.name == e._2.firstDepartureStation.name)(
            _.putTrain(e._1, Forward)
          ).getOrElse(stationsEE)
        )
    extension (timeTables: Seq[ListMap[Station, TrainStationTime]])
      def orderSchedule(): Seq[List[(Station, TrainStationTime)]] =
        timeTables.map(t =>
          t.toList.map(e => (e, e._2.departure)).flatMap(e =>
            e match
              case (el, Some(value)) => Some((el, value))
              case _                 => None
          ).sortBy(_._2).map(_._1)
        )

    extension (timeTables: Seq[List[(Station, TrainStationTime)]])
      @SuppressWarnings(Array("org.wartremover.warts.IterableOps"))
      def firstDepartureStation: Station =
        timeTables.flatMap(_.headOption.map(e => (e._1, e._2.departure))).flatMap {
          case (station, Some(clockTime)) => Some((station, clockTime))
          case (_, None)                  => None
        }.minBy(_._2)._1

    def empty(): RailwayEnvironment =
      apply(Seq[Station](), Seq[Route](), Seq[Train](), Seq[Timetable]())

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

    private final case class SimulationEnvironmentImpl(
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement]
    ) extends RailwayEnvironment:

      def doStep(dt: Int): RailwayEnvironment =
        // Allow agents to be at the same time in more than an environment element
        // that because an agent when enters a station doesn't leave immediately the route and vice versa
        // also for future improvements, an agent when crossing two rails will be in two rails at the same time
        val agentsWithActions = agents.map(a => a -> a.doStep(dt, this))
        agentsWithActions.foldLeft(this) { (env, agentWithAction) =>
          agentWithAction match
            case (agent: TrainAgent, Some(Actions.MoveBy(d))) =>
              // Update Idea: startByMovingAgent
              // If is already on a route,
              //  could remain completely on the route
              //  or enter the station
              //  or completely leave the route
              // If is already on a station,
              //  could remain completely on the station
              //      (for now is not possible since station doesn't take into account rail platform track length)
              //  or enter the route
              //  or completely leave the station
              env.updateEnvironmentWith(agent.updateDistanceTravelled(d))
            case _ => this
        }

      private def updateEnvironmentWith(agent: TrainAgent): SimulationEnvironmentImpl =
        updateAgentOnRoute(agent).updateAgentInStation(agent)
      private def updateAgentOnRoute(agent: TrainAgent): SimulationEnvironmentImpl =
        agent.findIn(routes).fold(this)(ree => routeUpdateFunction(ree, agent))
      private def updateAgentInStation(agent: TrainAgent): SimulationEnvironmentImpl =
        agent.findIn(stations).fold(this)(see => copy(stations = stations.updateWhen(_ == see)(s => s)))

      private def routeUpdateFunction(route: RouteEnvironmentElement, agent: TrainAgent): SimulationEnvironmentImpl =
        agent.distanceTravelled match
          case d if d >= route.length + agent.length =>
            route.removeTrain(agent) match
              case Some(ree) => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree))
              case _         => this
          case d if d >= route.length =>
            (
              stations.find(_.name == route.arrival.name).flatMap(_.putTrain(agent, Forward)),
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
      ): SimulationEnvironmentImpl =
        agent.distanceTravelled match
          case d if d >= agent.length =>
            station.removeTrain(agent) match
              case Some(see) => copy(stations = stations.updateWhen(_.name == see.name)(_ => see))
              case _         => this
          case _ => station.updateTrain(agent) match
              case Some(see) => copy(stations = stations.updateWhen(_.name == see.name)(_ => see))
              case _         => this
