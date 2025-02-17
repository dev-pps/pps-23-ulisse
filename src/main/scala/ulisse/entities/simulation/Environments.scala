package ulisse.entities.simulation

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.Agents.SimulationAgent
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.TrainAgent

object Environments:

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
            if agent.travelDistance > 0 then
              findRoute(agent).map(route =>
                if agent.travelDistance + d >= route.length then
                  env.copy(agents = agents ++ Seq(agent.travelDistance = 0)).arriveToDestination(route, agent)
                else
                  env.copy(agents = agents ++ Seq(agent.updateTravelDistance(d)))
              ).getOrElse(env)
            else
              env.copy(agents = agents ++ Seq(agent.updateTravelDistance(d)))
          case (env, (agent, _)) => env.copy(agents = agents ++ Seq(agent))
        }
      private def findRoute(agent: TrainAgent): Option[RouteEnvironmentElement] =
        routes.find(_.trains.exists(_.contains(agent)))
      private def findStation(agent: TrainAgent): Option[StationEnvironmentElement] =
        stations.find(_.tracks.contains(agent))
//      private def leaveStation(station: StationEnvironmentElement, train: TrainAgent): SimulationEnvironment =
//        this.copy(stations = stations.map(s => if s == station then s.updateTrack(station.tracks.find(_.train.contains(train)).get, None) else s))
      private def arriveToDestination(route: Route, train: TrainAgent): SimulationEnvironmentImpl =
        this.copy(stations =
          stations.map(s =>
            if s.coordinate == route.arrival.coordinate then
              s.firstAvailableTrack.map(track => s.updateTrack(track, Some(train))).getOrElse(s)
            else s
          )
        )

      def stations_=(newStations: Seq[StationEnvironmentElement]): SimulationEnvironment =
        copy(stations = newStations)
      def routes_=(newRoutes: Seq[RouteEnvironmentElement]): SimulationEnvironment =
        copy(routes = newRoutes)
      def agents_=(newAgents: Seq[SimulationAgent]): SimulationEnvironment =
        copy(agents = newAgents)
