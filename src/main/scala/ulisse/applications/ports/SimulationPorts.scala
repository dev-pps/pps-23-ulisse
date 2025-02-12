package ulisse.applications.ports

import ulisse.entities.simulation.Simulations.{EngineState, SimulationData}

import scala.concurrent.Future

object SimulationPorts:

  trait Output:
    def stepNotification(data: SimulationData): Unit

  trait Input:
    def start(): Future[EngineState]
    def stop(): Future[EngineState]
    def reset(): Future[EngineState]
