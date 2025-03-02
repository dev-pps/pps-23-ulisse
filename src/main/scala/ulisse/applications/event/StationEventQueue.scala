package ulisse.applications.event
import ulisse.applications.AppState
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the station. */
trait StationEventQueue:
  /** Add an event to read a station. */
  def addReadStationEvent(event: StationManager => Unit): Unit

  /** Add an event to create a station. */
  def addCreateStationEvent(event: StationManager => StationManager): Unit

  /** Add an event to update a station. */
  def addUpdateStationEvent(event: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): Unit

  /** Add an event to delete a station. */
  def addDeleteStationEvent(event: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): Unit = addUpdateStationEvent(event)

/** Companion object for the [[StationEventQueue]] trait. */
object StationEventQueue:

  /** Create a new station event queue. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): StationEventQueue = StationEventQueueImpl(events)

  /** Implementation of the station event queue. */
  private case class StationEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends StationEventQueue:

    override def addReadStationEvent(event: StationManager => Unit): Unit = events.offer(_ readStation event)

    override def addCreateStationEvent(event: StationManager => StationManager): Unit =
      events.offer(_ updateStation event)

    override def addUpdateStationEvent(event: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit = events.offer(_ updateStationSchedule event)
