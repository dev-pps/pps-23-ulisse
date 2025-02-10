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
  def apply(simulationEvents: LinkedBlockingQueue[SimulationState => SimulationState]): SimulationManager =
    SimulationManagerImpl(simulationEvents, false, 0)
  private case class SimulationManagerImpl(
      simulationEvents: LinkedBlockingQueue[SimulationState => SimulationState],
      running: Boolean,
      step: Int
  ) extends SimulationManager:
    override def start(): SimulationManager =
      doStep(); copy(running = true)
    override def stop(): SimulationManager =
      copy(running = false)
    override def reset(): SimulationManager = copy(running = false, step = 0)

    override def doStep(): SimulationManager =
      simulationEvents.offer((state: SimulationState) => {
        if state.simulationManager.running then
          state.copy(simulationManager = state.simulationManager.doStep())
        else
          state
      })
      copy(step = step + 1)
