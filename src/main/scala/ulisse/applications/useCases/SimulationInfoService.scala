package ulisse.applications.useCases

import ulisse.applications.ports.SimulationInfoPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.{RouteEnvironmentElement, Routes}
import ulisse.entities.simulation.Environments.RailwayEnvironment
import ulisse.entities.simulation.SimulationAgent
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.train.Trains.Train
import ulisse.entities.train.{TrainAgent, Trains}
import ulisse.applications.AppState
import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

final case class SimulationInfoService(
    private val eventQueue: LinkedBlockingQueue[AppState => AppState]
) extends SimulationInfoPorts.Input:
  // TODO is best a future that fails or a future that return an option?
  override def stationInfo(s: Station): Future[Option[StationEnvironmentElement]] =
    val p = Promise[Option[StationEnvironmentElement]]
    eventQueue.add((state: AppState) => {
      p.success(state.simulationManager.simulationData.simulationEnvironment.stations.find(_.name == s.name))
      state
    })
    p.future

  override def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]] =
    val p = Promise[Option[RouteEnvironmentElement]]
    eventQueue.add((state: AppState) => {
      p.success(state.simulationManager.simulationData.simulationEnvironment.routes.find(_.id == r.id))
      state
    })
    p.future

  override def trainInfo(t: Train): Future[Option[TrainAgent]] =
    val p = Promise[Option[TrainAgent]]
    eventQueue.add((state: AppState) => {
      p.success(state.simulationManager.simulationData.simulationEnvironment.agents.collect({ case t: TrainAgent => t }
      ).find(_.name == t.name))
      state
    })
    p.future
