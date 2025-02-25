package ulisse.applications.ports

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentInfo}
import ulisse.entities.train.Trains.Train

import scala.concurrent.Future

object SimulationInfoPorts:
  trait Input:
    def stationInfo(s: Station): Future[Option[StationEnvironmentElement]]
    def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]]
    def trainInfo(t: Train): Future[Option[TrainAgentInfo]]
