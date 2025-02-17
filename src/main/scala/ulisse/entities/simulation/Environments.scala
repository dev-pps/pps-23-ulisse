package ulisse.entities.simulation

import ulisse.entities.Routes.Route
import ulisse.entities.simulation.Agents.{SimulationAgent, TrainAgent}
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.StationEnvironmentElement

object Environments:
  trait RouteEnvironmentElement extends Route:
    val trains: Seq[Seq[TrainAgent]]

  trait SimulationEnvironment:
    def doStep(dt: Int): SimulationEnvironment
    def stations: Seq[StationEnvironmentElement]
    def routes: Seq[RouteEnvironmentElement]
    def agents: Seq[SimulationAgent]
    def stations_=(newStations: Seq[StationEnvironmentElement]): SimulationEnvironment
    def routes_=(newRoutes: Seq[RouteEnvironmentElement]): SimulationEnvironment
    def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment

  object SimulationEnvironment:
    def apply(
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        agents: Seq[SimulationAgent]
    ): SimulationEnvironment =
      SimulationEnvironmentImpl(stations, routes, agents)
    def empty(): SimulationEnvironment =
      apply(Seq[StationEnvironmentElement](), Seq[RouteEnvironmentElement](), Seq[SimulationAgent]())

    private final case class SimulationEnvironmentImpl(
        stations: Seq[StationEnvironmentElement],
        routes: Seq[RouteEnvironmentElement],
        agents: Seq[SimulationAgent]
    ) extends SimulationEnvironment:
      def doStep(dt: Int): SimulationEnvironment =
        val stepActions = agents.map(a => a -> a.doStep(dt, this)).toMap
        stepActions.foldLeft(this.copy(agents = Seq())) {
          case (env, (agent: TrainAgent, Actions.MoveBy(d))) =>
            env.copy(agents = agents ++ Seq(agent.updateTravelDistance(d)))
          case (env, (agent, _)) => env.copy(agents = agents ++ Seq(agent))
        }
      private def findRoute(agent: TrainAgent): Option[RouteEnvironmentElement] =
        routes.find(_.id == agent.routeId)
      def stations_=(newStations: Seq[StationEnvironmentElement]): SimulationEnvironment =
        copy(stations = newStations)
      def routes_=(newRoutes: Seq[RouteEnvironmentElement]): SimulationEnvironment =
        copy(routes = newRoutes)
      def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment =
        copy(agents = newAgents)
