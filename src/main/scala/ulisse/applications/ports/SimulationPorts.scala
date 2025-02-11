package ulisse.applications.ports

import ulisse.entities.simulation.Simulations.SimulationData

import scala.concurrent.Future

object SimulationPorts:

  trait Output:
    def stepNotification(data: SimulationData): Unit

  trait Input:
    def start(): Future[Unit]
    def stop(): Future[Unit]
    def reset(): Future[Unit]
