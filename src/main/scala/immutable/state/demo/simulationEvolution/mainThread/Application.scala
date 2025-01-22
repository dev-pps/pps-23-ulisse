package immutable.state.demo.simulationEvolution.mainThread

import java.util.concurrent.LinkedBlockingQueue
import scala.concurrent.{Future, Promise}

object Application:

  case class Simulation(stateEventQueue: LinkedBlockingQueue[AppState => AppState]):

    def runStep(): Unit = stateEventQueue.offer((state: AppState) => {
      if state.simulationManager.running then
        println(s"Simulation step ${state.simulationManager.step}")
        Simulation(stateEventQueue).runStep()
        state.copy(simulationManager = state.simulationManager.copy(step = state.simulationManager.step + 1))
      else
        state
    })

  object Ports:

    object SimulationPorts:
      trait Input:
        def start(): Future[Unit]
        def stop(): Future[Unit]

  object Adapters:

    case class SimulationInputAdapter(stateEventQueue: LinkedBlockingQueue[AppState => AppState])
        extends Ports.SimulationPorts.Input:
      def start(): Future[Unit] =
        val promise = Promise[Unit]()
        stateEventQueue.offer((state: AppState) => {
          println("Starting simulation")
          val newState = state.copy(simulationManager = state.simulationManager.start())
          Simulation(stateEventQueue).runStep()
          promise.success(())
          newState
        })
        promise.future

      def stop(): Future[Unit] =
        val promise = Promise[Unit]()
        stateEventQueue.offer((state: AppState) => {
          println("Stopping simulation")
          val newState = state.copy(simulationManager = state.simulationManager.stop())
          promise.success(())
          newState
        })
        promise.future

  case class SimulationManager(running: Boolean, step: Int):
    def stop(): SimulationManager  = copy(running = false)
    def start(): SimulationManager = copy(running = true)

  case class AppState(simulationManager: SimulationManager)
