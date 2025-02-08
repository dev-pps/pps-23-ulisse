package ulisse.adapters.output

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinates.Coordinate
import ulisse.entities.station.Station
import ulisse.infrastructures.view.simulation.SimulationPage

final case class SimulationNotificationAdapter[N: Numeric, C <: Coordinate[N], S <: Station[N, C]](
    simulationPage: SimulationPage
) extends SimulationPorts.Output:
  override def stepNotification(): Unit = simulationPage.updateData()
