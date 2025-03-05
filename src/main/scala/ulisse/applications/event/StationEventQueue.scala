package ulisse.applications.event
import ulisse.applications.AppState
import ulisse.applications.event.StationEventQueue.StationManagers
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the station. */
trait StationEventQueue:
  /** Add an event to read the stationManager. */
  def addReadStationManagerEvent(event: StationManager => Unit): Unit

  /** Add an event to update the stationManager. */
  def addUpdateStationManagerEvent(event: StationManager => StationManager): Unit

  /** Add an event to update the stationManagers. */
  def addUpdateStationManagersEvent(event: StationManagers => StationManagers): Unit

/** Companion object for the [[StationEventQueue]] trait. */
object StationEventQueue:
  /** Station managers. */
  final case class StationManagers(
      stationManager: StationManager,
      routeManager: RouteManager,
      timetableManager: TimetableManager
  )

  /** Create a new station event queue. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): StationEventQueue = StationEventQueueImpl(events)

  private case class StationEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends StationEventQueue:
    override def addReadStationManagerEvent(event: StationManager => Unit): Unit =
      events.offer(_ readStationManager event)
    override def addUpdateStationManagerEvent(event: StationManager => StationManager): Unit =
      events.offer(_ updateStationManager event)
    override def addUpdateStationManagersEvent(event: StationManagers => StationManagers): Unit =
      events.offer(_ updateStationManagers event)
