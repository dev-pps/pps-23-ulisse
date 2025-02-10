package ulisse.applications.managers

import ulisse.applications.SimulationState
import ulisse.applications.ports.SimulationPorts
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue

final case class SimulationEnvironment(
    stationMap: Seq[Station[?, ?]]
)

trait SimulationManager:
  val running: Boolean
  val step: Int
  def start(environment: SimulationEnvironment): SimulationManager
  def stop(): SimulationManager
  def reset(): SimulationManager
  def doStep(): SimulationManager

object SimulationManager:
  def apply(notificationService: SimulationPorts.Output): SimulationManager =
    SimulationManagerImpl(false, 0, SimulationEnvironment(Seq[Station[?, ?]]()), notificationService)
  private case class SimulationManagerImpl(
      running: Boolean,
      step: Int,
      environment: SimulationEnvironment,
      notificationService: SimulationPorts.Output
  ) extends SimulationManager:
    override def start(environment: SimulationEnvironment): SimulationManager =
      copy(running = true, environment = environment)
    override def stop(): SimulationManager  = copy(running = false)
    override def reset(): SimulationManager = copy(running = false, step = 0)
    override def doStep(): SimulationManager =
      notificationService.stepNotification()
      copy(step = step + 1)
