package ulisse.applications.ports

import ulisse.entities.simulation.data.{Engine, SimulationData}

import scala.concurrent.Future

object SimulationPorts:

  trait Output:
    /** Notify that a step has been completed */
    def stepNotification(data: SimulationData): Unit

    /** Notify that the simulation has ended */
    def simulationEnded(data: SimulationData): Unit

  trait Input:
    /** Setup simulation manager with AppState data,
      * should be called every time simulation page is opened
      * to retrieve also engine initial setup information
      */
    def initSimulation(): Future[(Engine, SimulationData)]

    /** Setup engine with step size and cycles per second, if cyclesPerSecond are none works in batch mode */
    def setupEngine(stepSize: Int, cyclesPerSecond: Option[Int]): Future[Option[Engine]]

    /** Start simulation. Note: the simulation must be initialized first */
    def start(): Future[Engine]

    /** Stop simulation */
    def stop(): Future[Engine]

    /** Reset simulation environment to the initialized state maintaining the engine configuration */
    def reset(): Future[Engine]
