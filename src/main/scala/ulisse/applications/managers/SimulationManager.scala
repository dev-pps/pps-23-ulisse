package ulisse.applications.managers

import ulisse.applications.SimulationState

import java.util.concurrent.LinkedBlockingQueue

trait SimulationManager:
  val running: Boolean
  val step: Int
  def start(): SimulationManager
  def stop(): SimulationManager
  def reset(): SimulationManager
  def doStep(): SimulationManager

object SimulationManager:
  def apply(): SimulationManager =
    SimulationManagerImpl(false, 0)
  private case class SimulationManagerImpl(
      running: Boolean,
      step: Int
  ) extends SimulationManager:
    override def start(): SimulationManager  = copy(running = true)
    override def stop(): SimulationManager   = copy(running = false)
    override def reset(): SimulationManager  = copy(running = false, step = 0)
    override def doStep(): SimulationManager = copy(step = step + 1)
