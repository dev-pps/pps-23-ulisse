package ulisse.applications.useCases

import ulisse.applications.ports.SimulationPorts

final case class SimulationService() extends SimulationPorts.Input:
  override def start(): Unit = println("Simulation started")
  override def stop(): Unit  = println("Simulation stopped")
  override def reset(): Unit = println("Simulation reset")
