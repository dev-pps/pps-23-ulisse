package ulisse.applications.managers

import ulisse.applications.SimulationState

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

trait SimulationManager:
  val running: Boolean
  val step: Int
  def start(): SimulationManager
  def stop(): SimulationManager
  def reset(): SimulationManager

object SimulationManager:
  def apply(simulationEvents: LinkedBlockingQueue[SimulationState => SimulationState]): SimulationManager = SimulationManagerImpl(simulationEvents, false, 0)
  private case class SimulationManagerImpl(simulationEvents: LinkedBlockingQueue[SimulationState => SimulationState], running: Boolean, step: Int) extends SimulationManager:
    override def start(): SimulationManager = copy(running = true)
    override def stop(): SimulationManager  = copy(running = false)
    override def reset(): SimulationManager = copy(running = false, step = 0)
    private def doStep(): Unit =
      simula
