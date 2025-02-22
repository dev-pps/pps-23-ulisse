package ulisse.entities.simulation

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.simulation.Simulations.Actions
import ulisse.entities.station.StationEnvironmentElement
import ulisse.entities.train.TrainAgent
import ulisse.entities.station.StationEnvironmentElement.*
import ulisse.entities.route.RouteEnvironmentElement.*
import ulisse.utils.CollectionUtils.*

object Environments:
  trait EnvironmentElement
  trait RailwayEnvironment:
    def doStep(dt: Int): RailwayEnvironment
    def stations: Seq[StationEnvironmentElement]
    def routes: Seq[RouteEnvironmentElement]
    def agents: Seq[SimulationAgent] =
      stations.flatMap(_.platforms.flatMap(_.train)) ++ routes.flatMap(_.tracks.flatMap(_.trains))
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
        val stepActions = agents.map(a => a -> a.doStep(dt, this)).toMap
//        stepActions.foldLeft(this) {
//          case (env, (agent: TrainAgent, Some(Actions.MoveBy(d)))) =>
//            if agent.isOnRoute then updateAgentOnRoute(agent, d)
//            else updateAgentInStation(agent, d)
//          case (env, (agent, _)) => env.copy(agents = agents ++ Seq(agent))
//        }
        this

      private def updateAgentOnRoute(agent: TrainAgent, distance: Double): SimulationEnvironmentImpl =
//        agent.findInRoutes(routes).fold(this)(route =>
//          val arriveAtDestination = agent.distanceTravelled + distance >= route.length
//          if arriveAtDestination then
//            copy(agents = agents ++ Seq(agent.resetDistanceTravelled())).arriveToDestination(route, agent)
//          else
//            copy(agents = agents ++ Seq(agent.updateDistanceTravelled(distance)))

//        )
        this

//      private def updateAgentInStation(agent: TrainAgent, distance: Double): SimulationEnvironmentImpl =
//        agent.findInStation(stations).fold(this)(station =>
//          copy(agents = agents ++ Seq(agent)).leaveStation(station, agent)
//        )

      private def leaveStation(station: StationEnvironmentElement, train: TrainAgent): SimulationEnvironmentImpl =
//        this.copy(stations = stations.updateWhen(_ == station)(train.leave))
        this
      private def arriveToDestination(route: Route, train: TrainAgent): SimulationEnvironmentImpl =
        // se arriva a destinazione, deve arrivare alla stazione di arrivo e togliersi dalla rotaia

//        this.copy(stations = stations.updateWhen(_.coordinate == route.arrival.coordinate)(train.arriveAt))
        this

      def stations_=(newStations: Seq[StationEnvironmentElement]): RailwayEnvironment =
        copy(stations = newStations)
      def routes_=(newRoutes: Seq[RouteEnvironmentElement]): RailwayEnvironment =
        copy(routes = newRoutes)
