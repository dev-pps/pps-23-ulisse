package ulisse

import ulisse.adapters.InputAdapterManager
import ulisse.applications.{AppState, EventQueue, InputPortManager}
import ulisse.infrastructures.view.GUI

object Main:

  @main def launchApp(): Unit =
    val eventQueue          = EventQueue()
    val inputPortManager    = InputPortManager(eventQueue)
    val inputAdapterManager = InputAdapterManager(inputPortManager)

    val map = GUI(inputAdapterManager)

    val initialState = AppState()
    eventQueue.startProcessing(initialState)
