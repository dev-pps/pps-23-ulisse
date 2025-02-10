package ulisse.applications.managers

trait SimulationManager:
  def start(): Unit
  def stop(): Unit
  def reset(): Unit

object SimulationManager:
  def apply(): SimulationManager = new SimulationManager:
    def start(): Unit = println("Simulation started")
    def stop(): Unit  = println("Simulation stopped")
    def reset(): Unit = println("Simulation reset")
