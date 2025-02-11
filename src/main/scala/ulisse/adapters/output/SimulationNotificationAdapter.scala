package ulisse.adapters.output

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinate
import ulisse.applications.useCases.SimulationData
import ulisse.entities.station.Station
import ulisse.infrastructures.view.simulation.SimulationPage

trait SimulationNotificationAdapterRequirements:
  def simulationPageComponent: SimulationPage

final case class SimulationNotificationAdapter(requirements: SimulationNotificationAdapterRequirements)
    extends SimulationPorts.Output with SimulationNotificationAdapterRequirements:
  override def simulationPageComponent: SimulationPage      = requirements.simulationPageComponent
  override def stepNotification(data: SimulationData): Unit = simulationPageComponent.updateData(data)
