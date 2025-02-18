package ulisse.entities.simulation

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.TrainAgent
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.utils.CollectionUtils.*

object Environments:
  trait EnvironmentElement
  trait RailwayEnvironment:
    def doStep(dt: Int): RailwayEnvironment
    def stations: Seq[StationEnvironmentElement]
    def routes: Seq[RouteEnvironmentElement]
    def agents: Seq[SimulationAgent]
    def stations_=(newStations: Seq[StationEnvironmentElement]): RailwayEnvironment
    def routes_=(newRoutes: Seq[RouteEnvironmentElement]): RailwayEnvironment
    def agents_=(newAgents: Seq[SimulationAgent]): RailwayEnvironment

  object RailwayEnvironment:
    def apply(
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        agents: Seq[SimulationAgent]
    ): RailwayEnvironment =
      SimulationEnvironmentImpl(stations, routes, agents)

    def empty(): RailwayEnvironment =
      apply(Seq[StationEnvironmentElement](), Seq[RouteEnvironmentElement](), Seq[SimulationAgent]())

    private final case class SimulationEnvironmentImpl(
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        agents: Seq[SimulationAgent]
    ) extends RailwayEnvironment:
      def doStep(dt: Int): RailwayEnvironment =
        val stepActions = agents.map(a => a -> a.doStep(dt, this)).toMap
        stepActions.foldLeft(this.copy(agents = Seq())) {
          case (env, (agent: TrainAgent, Some(Actions.MoveBy(d)))) =>
            if agent.distanceTravelled > 0 then
              findRoute(agent).map(route =>
                if agent.distanceTravelled + d >= route.length then
                  env.copy(agents = agents ++ Seq(agent.distanceTravelled = 0)).arriveToDestination(route, agent)
                else
                  env.copy(agents = agents ++ Seq(agent.updateDistanceTravelled(d)))
              ).getOrElse(env)
            else
              env.copy(agents = agents ++ Seq(agent.updateDistanceTravelled(d)))
          case (env, (agent, _)) => env.copy(agents = agents ++ Seq(agent))
        }
      private def findRoute(agent: TrainAgent): Option[RouteEnvironmentElement] =
        routes.find(_.trains.exists(_.contains(agent)))
      private def findStation(agent: TrainAgent): Option[StationEnvironmentElement] =
        stations.find(_.tracks.contains(agent))

      private def leaveStation(station: StationEnvironmentElement, train: TrainAgent): SimulationEnvironmentImpl =
        this.copy(stations = stations.updateWhen(_ == station)(train.leave))
      private def arriveToDestination(route: Route, train: TrainAgent): SimulationEnvironmentImpl =
        this.copy(stations = stations.updateWhen(_.coordinate == route.arrival.coordinate)(train.arriveAt))

      def stations_=(newStations: Seq[StationEnvironmentElement]): RailwayEnvironment =
        copy(stations = newStations)
      def routes_=(newRoutes: Seq[RouteEnvironmentElement]): RailwayEnvironment =
        copy(routes = newRoutes)
      def agents_=(newAgents: Seq[SimulationAgent]): RailwayEnvironment =
        copy(agents = newAgents)
