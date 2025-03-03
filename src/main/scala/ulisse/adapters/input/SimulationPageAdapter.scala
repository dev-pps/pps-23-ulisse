package ulisse.adapters.input

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.simulation.data.{EngineState, SimulationData}

import java.sql.Time
import scala.concurrent.Future

final case class SimulationPageAdapter(private val simulationPort: SimulationPorts.Input):
  def initSimulation(): Future[(EngineState, SimulationData)] = simulationPort.initSimulation()
  def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[EngineState]] =
    simulationPort.setupEngine(stepSize, cyclesPerSecond)
  def start(): Future[EngineState] = simulationPort.start()
  def stop(): Future[EngineState]  = simulationPort.stop()
  def reset(): Future[EngineState] = simulationPort.reset()
