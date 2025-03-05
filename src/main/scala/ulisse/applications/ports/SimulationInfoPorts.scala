package ulisse.applications.ports

import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironmentElement.StationEnvironmentInfo
import ulisse.entities.train.TrainAgents.TrainAgentInfo
import ulisse.entities.train.Trains.Train

import scala.concurrent.Future

/** Ports for Simulation Info */
object SimulationInfoPorts:
  trait Input:
    /** Get station info */
    def stationInfo(s: Station): Future[Option[StationEnvironmentInfo]]

    /** Get route info */
    def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]]

    /** Get train info */
    def trainInfo(t: Train): Future[Option[TrainAgentInfo]]
