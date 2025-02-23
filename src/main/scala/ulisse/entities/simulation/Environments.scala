package ulisse.entities.simulation

import ulisse.entities.route.{RouteEnvironmentElement, Track}
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.{Platform, Platform2, Station, StationEnvironmentElement}
import ulisse.entities.train.TrainAgent
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.route.RouteEnvironmentElement.*
import ulisse.utils.CollectionUtils.*

object Environments:
  trait TrainAgentsContainer[TAC <: TrainAgentsContainer[TAC]]:
    self: TAC =>
    def isAvailable: Boolean
    def putTrain(train: TrainAgent): Option[TAC]
    def updateTrain(train: TrainAgent): Option[TAC]
    def removeTrain(train: TrainAgent): Option[TAC]

  trait TrainAgentEEWrapper[EE <: TrainAgentEEWrapper[EE]]:
    self: EE =>
    type TAC <: TrainAgentsContainer[?]
    def putTrain(container: TAC, train: TrainAgent): Option[EE]
    def updateTrain(train: TrainAgent): Option[EE]
    def removeTrain(train: TrainAgent): Option[EE]

  trait RailwayEnvironment:
    def doStep(dt: Int): RailwayEnvironment
    def stations: Seq[StationEnvironmentElement]
    def routes: Seq[RouteEnvironmentElement]
    def agents: Seq[SimulationAgent] =
      (stations.flatMap(_.platforms.flatMap(_.train)) ++ routes.flatMap(_.tracks.flatMap(_.trains))).distinct
    def stations_=(newStations: Seq[StationEnvironmentElement]): RailwayEnvironment
    def routes_=(newRoutes: Seq[RouteEnvironmentElement]): RailwayEnvironment

  object RailwayEnvironment:
    // TODO evaluate where to do the initial placement of the trains
    def apply(
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        agents: Seq[SimulationAgent]
    ): RailwayEnvironment =
      SimulationEnvironmentImpl(stations, routes)

    def empty(): RailwayEnvironment =
      apply(Seq[StationEnvironmentElement](), Seq[RouteEnvironmentElement](), Seq[SimulationAgent]())

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
        agent.findInRoutes(routes).fold(this)(ree => routeUpdateFunction(ree, agent))
      private def updateAgentInStation(agent: TrainAgent): SimulationEnvironmentImpl =
        agent.findInStations(stations).fold(this)(see => copy(stations = stations.updateWhen(_ == see)(s => s)))

      private def routeUpdateFunction(route: RouteEnvironmentElement, agent: TrainAgent): SimulationEnvironmentImpl =
        agent.distanceTravelled match
          case d if d >= route.length + agent.length =>
            route.removeTrain(agent) match
              case Some(ree) => copy(routes = routes.updateWhen(_.id == ree.id)(_ => ree))
              case _         => this
          case d if d >= route.length =>
            (
              stations.find(_.name == route.arrival.name).flatMap(destination => agent.arriveAt(destination)),
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

      def stations_=(newStations: Seq[StationEnvironmentElement]): RailwayEnvironment =
        copy(stations = newStations)
      def routes_=(newRoutes: Seq[RouteEnvironmentElement]): RailwayEnvironment =
        copy(routes = newRoutes)
