package ulisse.adapters.output

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.infrastructures.view.simulation.SimulationPage

final case class SimulationNotificationAdapter() extends SimulationPorts.Output:
  @SuppressWarnings(Array("org.wartremover.warts.Var"))
  var simulationPage: Option[SimulationPage] = None
  override def stepNotification(): Unit      = for sp <- simulationPage do sp.updateData()
