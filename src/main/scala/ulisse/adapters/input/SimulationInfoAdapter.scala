package ulisse.adapters.input

import ulisse.applications.ports.SimulationInfoPorts
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.{Station, StationEnvironmentElement}
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentInfo}
import ulisse.entities.train.Trains.Train

import scala.concurrent.Future

final case class SimulationInfoAdapter(private val simulationInfoPort: SimulationInfoPorts.Input):
  def stationInfo(s: Station): Future[Option[StationEnvironmentElement]] = simulationInfoPort.stationInfo(s)
  def routeInfo(r: Route): Future[Option[RouteEnvironmentElement]]       = simulationInfoPort.routeInfo(r)
  def trainInfo(t: Train): Future[Option[TrainAgentInfo]]                = simulationInfoPort.trainInfo(t)
