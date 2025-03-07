package ulisse

import ulisse.adapters.InputAdapterManager
import ulisse.applications.{AppState, EventQueue, InputPortManager}
import ulisse.infrastructures.view.GUI

object Main:

  def main(args: Array[String]): Unit =
    launchApp()

  @main def launchApp(): Unit =
    val eventQueue          = EventQueue()
    val inputPortManager    = InputPortManager(eventQueue)
    val inputAdapterManager = InputAdapterManager(inputPortManager)

    val map = GUI(inputAdapterManager)

    val initialState = AppState()
    eventQueue.startProcessing(initialState)
