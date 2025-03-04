package ulisse.applications.useCases

import ulisse.applications.event.SimulationEventQueue
import ulisse.applications.ports.SimulationInfoPorts
import ulisse.entities.route.Routes.Route
import ulisse.entities.route.{RouteEnvironmentElement, Routes}
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironments.{StationEnvironmentElement, StationEnvironmentInfo}
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.entities.train.Trains
import ulisse.entities.train.Trains.Train
import ulisse.utils.Times.Time

import scala.concurrent.{Future, Promise}

final case class SimulationInfoService(private val eventQueue: SimulationEventQueue) extends SimulationInfoPorts.Input:

  override def stationInfo(s: Station): Future[Option[StationEnvironmentInfo]] =
    val p = Promise[Option[StationEnvironmentInfo]]
    eventQueue.readSimulationEnvironment(env =>
      (
        env.stations.find(_ == s),
        Time.secondsToOverflowTime(env.timetables.flatMap(_.delayIn(s)).foldLeft(0)(_ + _.toSeconds))
      ) match
        case (Some(see), t) => p.success(Some(StationEnvironmentInfo(see, t)))
        case _              => p.success(None)
    )
    p.future

  override def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]] =
    val p = Promise[Option[RouteEnvironmentElement]]
    eventQueue.readSimulationEnvironment(env =>
      p.success(env.routes.find(_ === r))
    )
    p.future

  override def trainInfo(t: Train): Future[Option[TrainAgentInfo]] =
    val p = Promise[Option[TrainAgentInfo]]
    eventQueue.readSimulationEnvironment(env => {
      (env.trains.find(_ == t), env.timetablesByTrain.find(_._1 == t)) match
        case (Some(t), Some(tt)) => p.success(Some(TrainAgentInfo(t, tt._2)))
        case _                   => p.success(None)
    })
    p.future
