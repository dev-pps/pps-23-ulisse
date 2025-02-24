package ulisse.adapters.input

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.simulation.Simulations.EngineState

import scala.concurrent.Future

final case class SimulationPageAdapter(private val simulationPort: SimulationPorts.Input):
  def start(): Future[EngineState] = simulationPort.start()
  def stop(): Future[EngineState]  = simulationPort.stop()
  def reset(): Future[EngineState] = simulationPort.reset()
