package ulisse.applications.ports

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.station.Station
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentInfo}
import ulisse.entities.train.Trains.Train

import scala.concurrent.Future

object SimulationInfoPorts:
  trait Input:
    def stationInfo(s: Station): Future[Option[StationEnvironmentElement]]
    def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]]
    // TODO timetables are not really implemented => default empty list
    def trainInfo(t: Train): Future[Option[TrainAgentInfo]]
