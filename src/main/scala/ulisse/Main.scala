package ulisse

import ulisse.adapters.InputAdapterManager
import ulisse.adapters.input.SimulationPageAdapter
import ulisse.adapters.output.SimulationNotificationAdapter
import ulisse.applications.ports.SimulationPorts
import ulisse.applications.useCases.SimulationService
import ulisse.applications.{AppState, EventQueue, InputPortManager}
import ulisse.infrastructures.utilty.{SimulationNotificationAdapterRequirements, SimulationNotificationBridge}
import ulisse.infrastructures.view.GUI
import ulisse.infrastructures.view.page.workspaces.SimulationWorkspace
import ulisse.infrastructures.view.simulation.SimulationNotificationListener

object Main:

  def main(args: Array[String]): Unit =
    launchApp()

  private final case class SimulationSetting(eventQueue: EventQueue):
    private val simulationBridge: SimulationNotificationBridge = SimulationNotificationBridge(() => workspace)
    private val simulationOutput: SimulationPorts.Output       = SimulationNotificationAdapter(simulationBridge)
    val simulationInput: SimulationPorts.Input                 = SimulationService(eventQueue, simulationOutput)
    val simulationAdapter: SimulationPageAdapter               = SimulationPageAdapter(simulationInput)
    val workspace: SimulationWorkspace                         = SimulationWorkspace(simulationAdapter)

  @main def launchApp(): Unit =
    val eventQueue = EventQueue()

    val simulationSetting   = SimulationSetting(eventQueue)
    val inputPortManager    = InputPortManager(eventQueue, simulationSetting.simulationInput)
    val inputAdapterManager = InputAdapterManager(inputPortManager, simulationSetting.simulationAdapter)

    val map = GUI(inputAdapterManager, simulationSetting.workspace)

    val initialState = AppState()
    eventQueue.startProcessing(initialState)
