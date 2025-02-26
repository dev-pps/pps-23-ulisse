package ulisse.applications.ports

import ulisse.entities.simulation.Simulations.{EngineState, SimulationData}
import ulisse.utils.Times.{ClockTime, Time}

import scala.concurrent.Future

object SimulationPorts:

  trait Output:
    def stepNotification(data: SimulationData): Unit
    def simulationEnded(data: SimulationData): Unit

  trait Input:
    /** Setup simulation manager with AppState data,
      * should be called every time simulation page is opened
      * to retrieve also engine initial setup information
      */
    def initSimulation(): Future[(EngineState, SimulationData)]

    /** Setup engine with step size and cycles per second, if cyclesPerSecond are none works in batch mode */
    def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[EngineState]]

    /** Start simulation. Note: the simulation must be initialized first */
    def start(): Future[EngineState]

    /** Stop simulation */
    def stop(): Future[EngineState]

    /** Reset simulation environment to the initialized state maintaining the engine configuration */
    def reset(): Future[EngineState]
