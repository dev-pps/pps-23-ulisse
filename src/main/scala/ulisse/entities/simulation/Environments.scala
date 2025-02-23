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
      (stations.flatMap(_.platforms.flatMap(_.train)) ++ routes.flatMap(_.tracks.flatMap(_.trains))).distinct
    def stations_=(newStations: Seq[StationEnvironmentElement]): RailwayEnvironment
    def routes_=(newRoutes: Seq[RouteEnvironmentElement]): RailwayEnvironment

  @SuppressWarnings(Array("org.wartremover.warts.TripleQuestionMark"))
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
        agentsWithActions.foreach {
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

            //                  findInRoutes(routes).fold(this)(route =>
//                    val arriveAtDestination = agent.distanceTravelled + d >= route.length
//                    if arriveAtDestination then
//                      copy(agents = agents ++ Seq(agent.resetDistanceTravelled())).arriveToDestination(route, agent)
//                    else
//                      copy(agents = agents ++ Seq(agent.updateDistanceTravelled(d)))
//                  )
//                def updateStateInStation: SimulationEnvironmentImpl =
//                  findInStation(stations).fold(this)(station =>
//                    copy(agents = agents ++ Seq(agent)).leaveStation(station, agent)
//                  )

//              if agent.isOnStation then updateAgentInStation(agent)
//              else updateAgentInStation(agent, d)
//              agent.findInRoutes(routes).map(route =>
//                if agent.distanceTravelled + agent.length >= route.length then
//                  route.removeTrain(agent)
//                else
//                  route.updateTrain(agent)
//              )
//              agent.findInStation(stations).map(station =>
//                station.firstAvailablePlatform.map(platform =>
//                  station.updatePlatform(platform, Some(agent))
//                )
//              )
            this
//                if agent.isOnRoute then updateAgentOnRoute(agent, d)
//                else updateAgentInStation(agent, d)
          case _ => this
        }
//          case (agent: TrainAgent, Some(Actions.MoveBy(d))) =>
//              if agent.isOnRoute then updateAgentOnRoute(agent, d)
//              else updateAgentInStation(agent, d)
//          case (agent, _) => this
//        )
//
        // if an agent is on a station and wants to move, it should move to the route
        val actionForTrainsInStation =
          stations.flatMap(_.platforms.flatMap(_.train)).map(tis => tis -> tis.doStep(dt, this))
        // if an agent is on a route and wants to move, it should move on the route => if it arrives at the end of the route, it should move to the station
        val actionForTrainsOnRoute = routes.flatMap(_.tracks.flatMap(_.trains)).map(tor => tor -> tor.doStep(dt, this))
//        val stepActions = agents.map(a => a -> a.doStep(dt, this)).toMap
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
