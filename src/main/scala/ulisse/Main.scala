package ulisse

import ulisse.applications.{AppState, EventQueue, ServicesManager}
import ulisse.infrastructures.view.GUIView

object Main:

  @main def launchApp(): Unit =
    val eventQueue      = EventQueue()
    val servicesManager = ServicesManager(eventQueue)
    val map             = GUIView()

    val initialState = AppState()
    eventQueue.startProcessing(initialState)
