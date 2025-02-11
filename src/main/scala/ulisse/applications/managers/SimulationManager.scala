package ulisse.applications.managers

import ulisse.applications.SimulationState
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.useCases.{SimulationAgent, SimulationData}
import ulisse.entities.simulation.Environments.SimulationEnvironment
import ulisse.entities.station.Station

import java.util.concurrent.LinkedBlockingQueue

trait SimulationManager:
  val running: Boolean
  val step: Int
  def start(environment: SimulationEnvironment): SimulationManager
  def stop(): SimulationManager
  def reset(): SimulationManager
  def doStep(): SimulationManager

object SimulationManager:
  def apply(notificationService: SimulationPorts.Output): SimulationManager =
    SimulationManagerImpl(false, 0, SimulationEnvironment(Seq[Station]()), notificationService)
    SimulationManagerImpl(
      false,
      0,
      SimulationEnvironment(Seq[Station]()),
      notificationService,
      0.0,
      0,
      SimulationAgent(1, 0, 5)
    )
  private case class SimulationManagerImpl(
      running: Boolean,
      step: Int,
      environment: SimulationEnvironment,
      notificationService: SimulationPorts.Output,
      secondElapsed: Double,
      lastUpdate: Long,
      simulationAgent: SimulationAgent
  ) extends SimulationManager:
    override def start(environment: SimulationEnvironment): SimulationManager =
      copy(running = true, environment = environment, lastUpdate = System.currentTimeMillis())
    override def stop(): SimulationManager = copy(running = false)
    override def reset(): SimulationManager =
      copy(running = false, step = 0, secondElapsed = 0, lastUpdate = 0, simulationAgent = SimulationAgent(1, 0, 5))
    override def doStep(): SimulationManager =
      val updatedStep          = step + 1
      val lu                   = System.currentTimeMillis()
      val deltaElapsed         = lu - lastUpdate
      val updatedAgent         = simulationAgent.update(deltaElapsed / 1000.0)
      val updatedSecondElapsed = secondElapsed + deltaElapsed / 1000.0
      println(s"[SimulationManager]: Step $lastUpdate, $deltaElapsed")
      notificationService.stepNotification(SimulationData(updatedStep, updatedSecondElapsed, updatedAgent))
      copy(step = updatedStep, secondElapsed = updatedSecondElapsed, lastUpdate = lu, simulationAgent = updatedAgent)
