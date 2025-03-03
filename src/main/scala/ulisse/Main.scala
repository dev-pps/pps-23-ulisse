package ulisse

import ulisse.applications.{AppState, EventQueue, InputPortManager}
import ulisse.infrastructures.view.GUIView

object Main:

  @main def launchApp(): Unit =
    val eventQueue       = EventQueue()
    val inputPortManager = InputPortManager(eventQueue)
    val map              = GUIView(inputPortManager)

    val initialState = AppState()
    eventQueue.startProcessing(initialState)
