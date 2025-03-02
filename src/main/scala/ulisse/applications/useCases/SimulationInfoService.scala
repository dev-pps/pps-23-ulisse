package ulisse.applications.useCases

import ulisse.applications.EventQueues.SimulationEventQueue
import ulisse.applications.ports.SimulationInfoPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.{RouteEnvironmentElement, Routes}
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.Trains
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentInfo}

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class SimulationInfoService(private val eventQueue: SimulationEventQueue) extends SimulationInfoPorts.Input:

  override def stationInfo(s: Station): Future[Option[StationEnvironmentElement]] =
    val p = Promise[Option[StationEnvironmentElement]]
    eventQueue.addReadSimulationEvent(simulationData =>
      p.success(simulationData.simulationEnvironment.stations.find(_.name == s.name))
    )
    p.future

  override def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]] =
    val p = Promise[Option[RouteEnvironmentElement]]
    eventQueue.addReadSimulationEvent(simulationData =>
      p.success(simulationData.simulationEnvironment.routes.find(_.id == r.id))
    )
    p.future

  override def trainInfo(t: Train): Future[Option[TrainAgentInfo]] =
    val p = Promise[Option[TrainAgentInfo]]
    // TODO find timetables
    eventQueue.addReadSimulationEvent(simulationData => {
      p.success(simulationData.simulationEnvironment.agents.collect({ case t: TrainAgent => t }).find(
        _.name == t.name
      ).map(TrainAgentInfo(_, List.empty)))
    })
    p.future
