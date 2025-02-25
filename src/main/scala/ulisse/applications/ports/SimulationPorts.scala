package ulisse.applications.ports

import ulisse.entities.simulation.Simulations.{EngineState, SimulationData}
import ulisse.utils.Times.{ClockTime, Time}

import scala.concurrent.Future

object SimulationPorts:

  trait Output:
    def stepNotification(data: SimulationData): Unit

  trait Input:
    def initSimulation(): Future[EngineState]
    def setupEngine(stepSize: Time, cyclesPerSecond: Option[Int]): Future[EngineState]
    def start(): Future[EngineState]
    def stop(): Future[EngineState]
    def reset(): Future[EngineState]
