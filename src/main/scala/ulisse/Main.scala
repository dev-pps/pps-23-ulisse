package ulisse

import ulisse.adapters.InputAdapterManager
import ulisse.adapters.input.{SimulationInfoAdapter, SimulationPageAdapter}
import ulisse.adapters.output.{SimulationNotificationAdapter, SimulationNotificationListener}
import ulisse.applications.ports.{SimulationInfoPorts, SimulationPorts}
import ulisse.applications.useCases.{SimulationInfoService, SimulationService}
import ulisse.applications.{AppState, EventQueue, InputPortManager}
import ulisse.infrastructures.utilty.SimulationNotificationBridge
import ulisse.infrastructures.view.GUI
import ulisse.infrastructures.view.page.workspaces.SimulationWorkspace

object Main:

  def main(args: Array[String]): Unit =
    launchApp()

  private final case class SimulationSetting(eventQueue: EventQueue):
    private val simulationBridge: SimulationNotificationBridge = SimulationNotificationBridge(() => workspace)
    private val simulationOutput: SimulationPorts.Output       = SimulationNotificationAdapter(simulationBridge)
    val simulationInput: SimulationPorts.Input                 = SimulationService(eventQueue, simulationOutput)
    val simulationInfoInput: SimulationInfoPorts.Input         = SimulationInfoService(eventQueue)
    val simulationAdapter: SimulationPageAdapter               = SimulationPageAdapter(simulationInput)
    val simulationInfoAdapter: SimulationInfoAdapter           = SimulationInfoAdapter(simulationInfoInput)

    val workspace: SimulationWorkspace = SimulationWorkspace(simulationAdapter, simulationInfoAdapter)

  @main def launchApp(): Unit =
    val eventQueue      = EventQueue()
    val simulationSetup = SimulationSetting(eventQueue)
    val inputPortManager =
      InputPortManager(eventQueue, simulationSetup.simulationInput, simulationSetup.simulationInfoInput)
    val inputAdapterManager =
      InputAdapterManager(inputPortManager, simulationSetup.simulationAdapter, simulationSetup.simulationInfoAdapter)

    val gui = GUI(inputAdapterManager, simulationSetup.workspace)

    eventQueue.startProcessing(AppState())
