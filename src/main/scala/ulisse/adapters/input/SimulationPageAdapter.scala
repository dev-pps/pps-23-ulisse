package ulisse.adapters.input

import ulisse.applications.ports.SimulationPorts

final case class SimulationPageAdapter(simulationPort: SimulationPorts.Input):
  def start(): Unit = simulationPort.start()
  def stop(): Unit  = simulationPort.stop()
  def reset(): Unit = simulationPort.reset()
