package ulisse.adapters.input

import ulisse.applications.ports.SimulationPorts
import ulisse.entities.simulation.data.{Engine, SimulationData}

import java.sql.Time
import scala.concurrent.Future

final case class SimulationPageAdapter(private val simulationPort: SimulationPorts.Input):
  def initSimulation(): Future[(Engine, SimulationData)] = simulationPort.initSimulation()
  def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[Engine]] =
    simulationPort.setupEngine(stepSize, cyclesPerSecond)
  def start(): Future[Engine] = simulationPort.start()
  def stop(): Future[Engine]  = simulationPort.stop()
  def reset(): Future[Engine] = simulationPort.reset()
