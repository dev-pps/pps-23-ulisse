package ulisse.adapters.output

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.Coordinate
import ulisse.entities.simulation.Simulations.SimulationData
import ulisse.entities.station.Station
import ulisse.infrastructures.view.simulation.SimulationPage

final case class SimulationNotificationAdapter(private val simulationPage: SimulationPage)
    extends SimulationPorts.Output:
  override def stepNotification(data: SimulationData): Unit = simulationPage.updateData(data)
  override def simulationEnded(data: SimulationData): Unit  = simulationPage.endSimulation(data)
