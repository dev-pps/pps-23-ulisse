package ulisse.entities.simulation

object Agents:
  trait SimulationAgent:
    def timeStep: Double
    def update(): SimulationAgent
