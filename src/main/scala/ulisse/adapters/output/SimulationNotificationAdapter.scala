package ulisse.adapters.output

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.simulation.SimulationPage

/** Adapter for the SimulationPorts.Output port. */
final case class SimulationNotificationAdapter(private val simulationPage: SimulationPage*)
    extends SimulationPorts.Output:
  override def stepNotification(data: SimulationData): Unit = simulationPage.foreach(_.updateData(data))
  override def simulationEnded(data: SimulationData): Unit  = simulationPage.foreach(_.endSimulation(data))
