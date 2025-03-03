package ulisse

import ulisse.applications.{AppState, EventQueue}
import ulisse.infrastructures.view.GUIView

object Main:

  @main def launchApp(): Unit =
    val eventQueue   = EventQueue()
    val initialState = AppState()
    val map          = GUIView()
