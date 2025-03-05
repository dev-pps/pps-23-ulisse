package ulisse.adapters.input

import ulisse.applications.ports.SimulationInfoPorts
import ulisse.entities.route.RouteEnvironmentElement
import ulisse.entities.route.Routes.Route
import ulisse.entities.station.Station
import ulisse.entities.station.StationEnvironments.StationEnvironmentElement
import ulisse.entities.train.TrainAgents.{TrainAgent, TrainAgentInfo}
import ulisse.entities.train.Trains.Train

import scala.concurrent.Future

/** Adapter for the SimulationInfoPorts.Input port. */
final case class SimulationInfoAdapter(private val simulationInfoPort: SimulationInfoPorts.Input):
  export simulationInfoPort.*
