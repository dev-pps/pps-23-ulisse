package ulisse.adapters.input

import ulisse.applications.ports.SimulationPorts

import scala.concurrent.Future

final case class SimulationPageAdapter(simulationPort: SimulationPorts.Input):
  def start(): Future[Unit] = simulationPort.start()
  def stop(): Future[Unit]  = simulationPort.stop()
  def reset(): Future[Unit] = simulationPort.reset()
