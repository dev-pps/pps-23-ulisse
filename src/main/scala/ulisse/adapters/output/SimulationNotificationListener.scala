package ulisse.adapters.output

import ulisse.entities.simulation.data.SimulationData

/** Listener for simulation notifications. */
trait SimulationNotificationListener:

  /** Update the simulation data. */
  def updateData(data: SimulationData): Unit

  /** End the simulation. */
  def endSimulation(data: SimulationData): Unit
