package ulisse.infrastructures.utilty

import ulisse.entities.simulation.data.SimulationData
import ulisse.infrastructures.view.simulation.SimulationNotificationListener

/** Adapter requirements for the simulation notification bridge. */
trait SimulationNotificationAdapterRequirements:
  /** The actual component that has to receive notification. */
  def simulationPageComponent(): SimulationNotificationListener

/** Bridge for the simulation notification. Is needed to make lazy DI. */
final case class SimulationNotificationBridge(requirements: SimulationNotificationAdapterRequirements)
    extends SimulationNotificationListener:
  override def updateData(data: SimulationData): Unit    = requirements.simulationPageComponent().updateData(data)
  override def endSimulation(data: SimulationData): Unit = requirements.simulationPageComponent().endSimulation(data)
