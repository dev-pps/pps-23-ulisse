package ulisse.applications.event
import ulisse.applications.AppState
import ulisse.applications.managers.RouteManagers.RouteManager
import ulisse.applications.managers.StationManager
import ulisse.applications.managers.TimetableManagers.TimetableManager

import java.util.concurrent.LinkedBlockingQueue

/** Event queue to update the route. */
trait RouteEventQueue:

  /** Add an event to read a route. */
  def addReadRouteEvent(event: RouteManager => Unit): Unit

  /** Add an event to create a route. */
  def addCreateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit

  /** Add an event to update a route. */
  def addUpdateRouteEvent(update: (
      StationManager,
      RouteManager,
      TimetableManager
  ) => (StationManager, RouteManager, TimetableManager)): Unit

  /** Add an event to delete a route. */
  def addDeleteRouteEvent(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager)): Unit

/** Companion object for the [[RouteEventQueue]] trait. */
object RouteEventQueue:

  /** Create a new route event queue. */
  def apply(events: LinkedBlockingQueue[AppState => AppState]): RouteEventQueue = RouteEventQueueImpl(events)

  private case class RouteEventQueueImpl(events: LinkedBlockingQueue[AppState => AppState]) extends RouteEventQueue:

    override def addReadRouteEvent(event: RouteManager => Unit): Unit = events.offer(_ readRoute event)

    override def addCreateRouteEvent(update: (StationManager, RouteManager) => (StationManager, RouteManager)): Unit =
      events.offer(_ updateRailwayNetwork update)

    override def addUpdateRouteEvent(update: (
        StationManager,
        RouteManager,
        TimetableManager
    ) => (StationManager, RouteManager, TimetableManager)): Unit = events.offer(_ updateRailwayNetworkSchedule update)

    override def addDeleteRouteEvent(update: (RouteManager, TimetableManager) => (RouteManager, TimetableManager))
        : Unit = events.offer(_ updateRouteSchedule update)
