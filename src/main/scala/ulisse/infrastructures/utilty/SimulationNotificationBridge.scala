package ulisse.infrastructures.utilty

import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.simulation.SimulationPage

trait SimulationNotificationAdapterRequirements:
  def simulationPageComponent(): SimulationPage

final case class SimulationNotificationBridge(requirements: SimulationNotificationAdapterRequirements)
    extends SimulationPage:
  override def updateData(data: SimulationData): Unit    = requirements.simulationPageComponent().updateData(data)
  override def endSimulation(data: SimulationData): Unit = requirements.simulationPageComponent().endSimulation(data)
